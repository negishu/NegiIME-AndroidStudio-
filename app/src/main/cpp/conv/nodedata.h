#pragma once

#include "./global_utils.h"
#include "./utils.h"

#include "FreeList.h"

class Node {
public:
    Node     *prev;
    Node     *next;
    Node     *bnext;
    Node     *enext;
    U08 keysize;
    U08 begin_pos;
    U08 end_pos;
    U16 rid;
    U16 lid;
    U32 value;
    double cost;
    double tcost;
    std::string Yomi;
    std::string Word;
    Node() {
        Init();
    }
    inline void Init() {
        prev = NULL;
        next = NULL;
        bnext = NULL;
        enext = NULL;
        Yomi.clear();
        Word.clear();
        keysize = 0;
        rid = 0;
        lid = 0;
        begin_pos = 0;
        end_pos = 0;
        cost = 0;
        tcost = 0;
        value = 0;
    }
};

class NodeAllocatorInterface;

class NodeAllocatorData {
public:
    ~NodeAllocatorData() {
        clear();
    }
    bool has(const char *name) const {
        return (data_.find(name) != data_.end());
    }
    void erase(const char *name) {
        if (has(name)) {
            delete data_[name];
            data_.erase(name);
        }
    }
    void clear() {
        for (std::map<const char *, Data *>::iterator it = data_.begin();
        it != data_.end(); ++it) {
            delete it->second;
        }
        data_.clear();
    }
    template<typename Type> Type *get(const char *name) {
        if (!has(name)) {
            data_[name] = new Type;
        }
        return reinterpret_cast<Type *>(data_[name]);
    }
    class Data {
    public:
        virtual ~Data() {}
    };
private:
    std::map<const char *, Data *> data_;
};

class NodeAllocatorInterface {
public:
    NodeAllocatorInterface() : max_nodes_size_(8192) {}
    virtual ~NodeAllocatorInterface() {}
    virtual Node *NewNode() = 0;
    virtual size_t max_nodes_size() const {
        return max_nodes_size_;
    }
    virtual void set_max_nodes_size(size_t max_nodes_size) {
        max_nodes_size_ = max_nodes_size;
    }
    NodeAllocatorData *mutable_data() {
        return &data_;
    }
    const NodeAllocatorData &data() {
        return data_;
    }
private:
    size_t max_nodes_size_;
    NodeAllocatorData data_;
};

class NodeAllocator : public NodeAllocatorInterface {
public:
    NodeAllocator() : node_freelist_(2048) {}
    virtual ~NodeAllocator() {}
    virtual Node *NewNode() {
        Node *node = node_freelist_.Alloc();
        node->Init();
        return node;
    }
    void Free() {
        node_freelist_.Free();
    }
private:
    FreeList<Node> node_freelist_;
};
