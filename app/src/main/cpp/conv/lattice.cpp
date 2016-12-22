#include "Lattice.h"

inline const Node * Lattice::InitBOSNode(Lattice *lattice, const U08 length)
{
    Node *bos_node = lattice->NewNode();
    bos_node->rid = 0;  // 0 is reserved for EOS/BOS
    bos_node->lid = 0;  // 0 is reserved for EOS/BOS
    bos_node->keysize = 0;
    bos_node->cost = 0;
    bos_node->tcost = 0;
    bos_node->begin_pos = length;
    bos_node->end_pos = length;
    bos_node->enext = NULL;

    return bos_node;
}

inline const Node * Lattice::InitEOSNode(Lattice *lattice, const U08 length)
{
    Node *eos_node = lattice->NewNode();
    eos_node->rid = 0;  // 0 is reserved for EOS/BOS
    eos_node->lid = 0;  // 0 is reserved for EOS/BOS
    eos_node->keysize = 0;
    eos_node->cost = 0;
    eos_node->tcost = 0;
    eos_node->begin_pos = length;
    eos_node->end_pos = length;
    eos_node->bnext = NULL;

    return eos_node;
}

Lattice::Lattice() :node_allocator_(new NodeAllocator)
{
}

Lattice::~Lattice()
{
}

NodeAllocatorInterface* Lattice::node_allocator() const
{
    return node_allocator_.get();
}

Node* Lattice::NewNode()
{
    return node_allocator_->NewNode();
}

void Lattice::SetKey(const std::string &key)
{
    Clear();

    key_ = key;

    begin_nodes_.resize(64);
    end_nodes_.resize(64);

    std::fill(begin_nodes_.begin(), begin_nodes_.end(), static_cast<Node *>(NULL));
    std::fill(end_nodes_.begin(), end_nodes_.end(), static_cast<Node *>(NULL));

    end_nodes_[0] = (Node *)InitBOSNode(this, static_cast<const U08>(0));
    begin_nodes_[key_.size()] = (Node *)InitEOSNode(this, static_cast<const U08>(key_.size()));
}

const std::string& Lattice::key() const
{
    return key_;
}

const Node * Lattice::begin_nodes(size_t pos) const
{
    return begin_nodes_[pos];
}

const Node * Lattice::end_nodes(size_t pos) const
{
    return end_nodes_[pos];
}

const Node * Lattice::bos_nodes() const
{
    return end_nodes_[0];
}

const Node * Lattice::eos_nodes() const
{
    return begin_nodes_[key_.size()];
}

void Lattice::Insert(size_t pos, Node *node)
{
    for (Node *rnode = node; rnode != NULL; rnode = rnode->bnext) {

        const size_t end_pos = std::min(rnode->keysize + pos, key_.size());

        Node * p = end_nodes_[end_pos];

        rnode->begin_pos = static_cast<const U08>(pos);
        rnode->end_pos = static_cast<const U08>(end_pos);
        rnode->prev = NULL;
        rnode->next = NULL;
        rnode->tcost = 0;

        rnode->enext = end_nodes_[end_pos];
        end_nodes_[end_pos] = rnode;
    }

    if (begin_nodes_[pos] == NULL) {

        begin_nodes_[pos] = node;
    }
    else {

        for (Node *rnode = node; rnode != NULL; rnode = rnode->bnext) {

            Node * p = begin_nodes_[pos];

            if (rnode->bnext == NULL) {

                rnode->bnext = begin_nodes_[pos];
                begin_nodes_[pos] = node;

                break;
            }
        }
    }
}

void Lattice::Insert(const U08 nPos, std::vector<searcher::Candidate>& candidate)
{
    Node *resultNode = NULL;

    for (std::vector<searcher::Candidate>::const_iterator it = candidate.begin(); it != candidate.end(); ++it) {

        const searcher::Candidate& cand = (*it);

        Node* pNewNode = NewNode();

        pNewNode->lid = cand.lid;
        pNewNode->rid = cand.rid;
        pNewNode->cost = cand.wcost * cand.wcostfact;
        pNewNode->value = cand.wordid;

        pNewNode->Yomi = cand.Yomi;
        pNewNode->Word = cand.Word;

        pNewNode->keysize = cand.Yomi.length();

        pNewNode->bnext = resultNode;

        resultNode = pNewNode;
    }

    if (resultNode) {

        Insert(nPos, resultNode);
    }
}

void Lattice::Clear()
{
    key_.clear();
    begin_nodes_.clear();
    end_nodes_.clear();
    node_allocator_->Free();
}

bool Lattice::has_lattice() const
{
    return !begin_nodes_.empty();
}
