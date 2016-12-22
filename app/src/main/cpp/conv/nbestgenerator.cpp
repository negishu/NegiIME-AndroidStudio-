#include "NBestGenerator.h"
#include "Connector.h"
#include "NodeData.h"
#include "Lattice.h"

const int kFreeListSize = 512;
const int kCostDiff = 3453;

NBestGenerator::NBestGenerator(const Connector& connector) : connector_(connector), freelist_(kFreeListSize)
{
    agenda_.Reserve(kFreeListSize);
}

NBestGenerator::~NBestGenerator(void)
{
}

void NBestGenerator::Init(const Node *begin_node, const Node *end_node, const Lattice *lattice)
{
    Reset();

    begin_node_ = begin_node;
    end_node_ = end_node;
    lattice_ = lattice;

    if (lattice_ == NULL || !lattice_->has_lattice()) {

        return;
    }

    for (Node *node = (Node *)lattice_->begin_nodes(end_node_->begin_pos); node != NULL; node = node->bnext) {

        if (node == end_node_ || (node->lid != end_node_->lid && (node->tcost - end_node_->tcost) <= kCostDiff && node->prev != end_node_->prev)) {

            Agenda::QueueElement *eos = freelist_.Alloc();

            eos->node = node;
            eos->next = NULL;
            eos->fx = node->tcost;
            eos->gx = 0;
            eos->structure_gx = 0;
            eos->w_gx = 0;
            agenda_.Push(eos);
        }
    }
}

void NBestGenerator::Reset()
{
    agenda_.Clear();
    freelist_.Free();
}

bool NBestGenerator::MakeCandidate(Candidate& candidate, int cost, int structure_cost, int wcost, const std::vector<const Node *> nodes) const
{
    candidate.Init();
    if (nodes.size() > 0) {
        candidate.lid = nodes.front()->lid;
        candidate.rid = nodes.at(nodes.size() - 1)->rid;
        candidate.cost = cost;
        candidate.structure_cost = structure_cost;
        candidate.wcost = wcost;
        for (size_t i = 0; i < nodes.size(); ++i) {
            const Node *node = nodes[i];
            if (node->keysize == 0) break;
            candidate.values[candidate.nValuePos++] = node->value;
            candidate.word += node->Word;
        }
    }
    return true;
}

bool NBestGenerator::Next(Candidate& candidate)
{
    if (lattice_ == NULL || !lattice_->has_lattice()) {
        return false;
    }

    const int KMaxTrial = 512;

    int num_trials = 0;

    while (!agenda_.IsEmpty()) {

        const Agenda::QueueElement *top = agenda_.Top();

        agenda_.Pop();

        const Node *rnode = top->node;

        if (num_trials++ > KMaxTrial) {   // too many trials
            return false;
        }

        if (rnode->end_pos == 0 && begin_node_->end_pos == 0) {
            //return false;
        }

        if (rnode->end_pos == begin_node_->end_pos) {

            std::vector<const Node *> nodes;

            const Node *prev_node = top->node;

            for (const Agenda::QueueElement *elm = top->next; elm != NULL; elm = elm->next) {

                nodes.push_back(elm->node);

                prev_node = elm->node;
            }

            return MakeCandidate(candidate, top->gx, top->structure_gx, top->w_gx, nodes);
        }
        else {

            const Agenda::QueueElement *best_left_elm = NULL;

            const bool is_right_edge = rnode->begin_pos == end_node_->begin_pos;
            const bool is_left_edge = rnode->begin_pos == begin_node_->end_pos;

            if (is_right_edge) {

                for (Node *lnode = (Node *)lattice_->end_nodes(rnode->begin_pos); lnode != NULL; lnode = lnode->enext) {

                    const double transition_cost = GetTransitionCost(lnode, rnode);

                    if (transition_cost < 50000)
                    {
                        int cost_diff = transition_cost + (rnode->tcost - end_node_->tcost);
                        int structure_cost_diff = 0;
                        int wcost_diff = 0;

                        Agenda::QueueElement *elm = freelist_.Alloc();

                        elm->node = lnode;
                        elm->gx = cost_diff + top->gx;
                        elm->structure_gx = structure_cost_diff + top->structure_gx;
                        elm->w_gx = wcost_diff + top->w_gx;
                        elm->fx = lnode->tcost + elm->gx;
                        elm->next = top;

                        agenda_.Push(elm);
                    }
                }
            }
            else
                if (is_left_edge) {

                    for (Node *lnode = (Node *)lattice_->end_nodes(rnode->begin_pos); lnode != NULL; lnode = lnode->enext) {

                        const double transition_cost = GetTransitionCost(lnode, rnode);

                        if (transition_cost < 50000)
                        {
                            int cost_diff = (lnode->tcost - begin_node_->tcost) + transition_cost + rnode->cost;
                            int structure_cost_diff = 0;
                            int wcost_diff = rnode->cost;

                            Agenda::QueueElement *elm = freelist_.Alloc();

                            elm->node = lnode;
                            elm->gx = cost_diff + top->gx;
                            elm->structure_gx = structure_cost_diff + top->structure_gx;
                            elm->w_gx = wcost_diff + top->w_gx;
                            elm->fx = lnode->tcost + elm->gx;
                            elm->next = top;

                            if (best_left_elm == NULL || best_left_elm->fx > elm->fx) {
                                best_left_elm = elm;
                            }
                        }
                    }

                    if (best_left_elm != NULL) {
                        agenda_.Push(best_left_elm);
                    }
                }
                else {

                    for (Node *lnode = (Node *)lattice_->end_nodes(rnode->begin_pos); lnode != NULL; lnode = lnode->enext) {

                        const double transition_cost = GetTransitionCost(lnode, rnode);

                        if (transition_cost < 50000)
                        {
                            int cost_diff = transition_cost + rnode->cost;
                            int structure_cost_diff = transition_cost;
                            int wcost_diff = transition_cost + rnode->cost;

                            Agenda::QueueElement *elm = freelist_.Alloc();

                            elm->node = lnode;
                            elm->gx = cost_diff + top->gx;
                            elm->structure_gx = structure_cost_diff + top->structure_gx;
                            elm->w_gx = wcost_diff + top->w_gx;
                            elm->fx = lnode->tcost + elm->gx;
                            elm->next = top;

                            agenda_.Push(elm);
                        }
                    }
                }
        }
    }

    return false;
}

const double NBestGenerator::GetTransitionCost(const Node *lnode, const Node *rnode) const
{
    return connector_.GetTransitionCost(lnode->rid, rnode->lid);
}