#pragma once

#include "./global_utils.h"
#include "./utils.h"

#include "FreeList.h"

class Node;
class Connector;
class Lattice;

class Candidate {
public:
    std::string key;
    std::string word;
    U16 cost;
    U16 wcost;
    U16 structure_cost;
    U16 lid;
    U16 rid;
    U08 nValuePos;
    U32 values[32];
    void Init() {
        key.clear();
        word.clear();
        cost = 0;
        structure_cost = 0;
        wcost = 0;
        lid = 0;
        rid = 0;
        nValuePos = 0;
    }

    Candidate() { Init(); };
};

class NBestGenerator {

public:

    explicit NBestGenerator(const Connector& connector);
    virtual ~NBestGenerator();

    void Init(const Node *begin_node, const Node *end_node, const Lattice *lattice);

    void Reset();

    bool Next(Candidate& candidate);

private:

    bool MakeCandidate(Candidate& candidate, int cost, int structure_cost, int w_cost, const std::vector<const Node *> nodes) const;

    const double GetTransitionCost(const Node *lnode, const Node *rnode) const;

    class Agenda {
    public:
        Agenda() {}
        ~Agenda() {}

        struct QueueElement {
            const Node *node;
            const QueueElement *next;
            int fx;  // f(x) = h(x) + g(x): cost function for A* search
            int gx;  // g(x)
            // transition cost part of g(x).
            // Do not take the transition costs to edge nodes.
            int structure_gx;
            int w_gx;
        };
        class QueueElementComp {
        public:
            inline const bool operator()(const QueueElement *q1, const QueueElement *q2) const {
                return (q1->fx > q2->fx);
            }
        };

        const QueueElement *Top() const { return priority_queue_.front(); }
        bool IsEmpty() const { return priority_queue_.empty(); }
        void Clear() { priority_queue_.clear(); }
        void Reserve(int size) { priority_queue_.reserve(size); }
        inline void Push(const QueueElement *element) {
            priority_queue_.push_back(element);
            std::push_heap(priority_queue_.begin(), priority_queue_.end(), QueueElementComp());
        }
        inline void Pop() {
            std::pop_heap(priority_queue_.begin(), priority_queue_.end(), QueueElementComp());
            priority_queue_.pop_back();
        }
    private:
        std::vector<const QueueElement*> priority_queue_;
    };

    Agenda agenda_;

    FreeList<Agenda::QueueElement> freelist_;

    const Node *begin_node_;
    const Node *end_node_;
    const Lattice *lattice_;

    const Connector& connector_;
};
