#pragma once

#include "./global_utils.h"
#include "./utils.h"
#include "./bitvector.h"

class bitvectorbasedarray {
public:
    bitvectorbasedarray() {}
    void Open(const U08 *image);
    void Close();
    const U08 *Get(const U32 index, U32& wLength, U08& uStep) const;
private:
    bitvector index_;
    size_t base_length_;
    size_t step_length_;
    size_t num_entries_;
    const U08 *data_;
    U32 cache_index_[34835];
    U32 cache_length_[34835];
};
