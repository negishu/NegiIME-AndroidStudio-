#pragma once

#include ".\\global_utils.h"
#include ".\\SparseArrayImage.h"

class Connector
{
public:
    Connector();
    const double GetTransitionCost(const U16 rid, const U16 lid) const;
private:
    scoped_ptr<SparseArrayImage> array_image_;
    unsigned short _lsize;
    unsigned short _rsize;
};
