#pragma once
#include "./dictionary.h"
class suffixDictionary : public Dictionary
{
public:
    suffixDictionary();
    virtual ~suffixDictionary(void);
protected:
    virtual void Init();
};
