#include "Connector.h"

#include "./connections.h"

Connector::Connector()
{
    const unsigned short *image = reinterpret_cast<const unsigned short*>(kConnectionData_data);

    _lsize = image[2];
    _rsize = image[4];

    array_image_.reset(new SparseArrayImage((kConnectionData_data + 12), (kConnectionData_size - 12)));
}

const double Connector::GetTransitionCost(unsigned short lid, unsigned short rid) const
{
    const U32 pos = array_image_->Peek((rid + (_rsize * lid)));

    if (pos == SparseArrayImage::kInvalidValueIndex) {
        return INT_MAX;
    }

    const double cost = array_image_->GetValue(pos)*32.0;

    return cost;
}
