#pragma once
#include "./dictionary.h"
class wordDictionary : public Dictionary
{
public:
    wordDictionary();
    virtual ~wordDictionary(void);
protected:
    virtual void Init();
};
