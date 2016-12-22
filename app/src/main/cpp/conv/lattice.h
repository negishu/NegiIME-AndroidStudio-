#pragma once

#include "NodeData.h"
#include "searcher.h"

class Lattice {
public:

    const Node *InitBOSNode(Lattice *lattice, const U08 length);
    const Node *InitEOSNode(Lattice *lattice, const U08 length);

    NodeAllocatorInterface *node_allocator() const;
    Node * NewNode();

    void SetKey(const std::string &key);

    const std::string& key() const;

    const Node *begin_nodes(size_t pos) const;
    const Node *end_nodes(size_t pos) const;
    const Node *bos_nodes() const;
    const Node *eos_nodes() const;

    void Insert(size_t pos, Node *node);
    void Insert(const U08 nPos, std::vector<searcher::Candidate>& candidate);
    void Clear();

    bool has_lattice() const;

    Lattice();
    virtual ~Lattice();

private:

    std::string key_;
    std::vector<Node *> begin_nodes_;
    std::vector<Node *> end_nodes_;

    scoped_ptr<NodeAllocator> node_allocator_;
};
