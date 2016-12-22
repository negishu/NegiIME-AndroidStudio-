#pragma once

#include ".\\global_utils.h"

template <class T>
class FreeList {
public:
    void Reset() {
        chunk_index_ = current_index_ = 0;
    }
    void Free() {
        for (size_t i = 1; i < pool_.size(); ++i) {
            delete[] pool_[i];
        }
        if (pool_.size() > 1) {
            pool_.resize(1);
        }
        current_index_ = 0;
        chunk_index_ = 0;
    }
    T* Alloc() {
        return Alloc(static_cast<size_t>(1));
    }
    T* Alloc(size_t len) {
        if ((current_index_ + len) >= size_) {
            chunk_index_++;
            current_index_ = 0;
        }
        if (chunk_index_ == pool_.size()) {
            pool_.push_back(new T[size_]);
        }
        T* r = pool_[chunk_index_] + current_index_;
        current_index_ += len;
        return r;
    }
    void set_size(size_t size) {
        size_ = size;
    }
    explicit FreeList(size_t size) :current_index_(0), chunk_index_(0), size_(size) {
    }
    virtual ~FreeList() {
        for (size_t i = 0; i < pool_.size(); ++i) {
            delete[] pool_[i];
        }
    }
private:
    FreeList() {}
    std::vector<T *> pool_;
    size_t current_index_;
    size_t chunk_index_;
    size_t size_;
};
