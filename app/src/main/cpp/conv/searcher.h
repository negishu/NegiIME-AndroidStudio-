#pragma once

#include "./global_utils.h"
#include "./utils.h"

#include "./loudstrie.h"

class Dictionary;
class searcher
{
public:
    class Candidate {
    public:
        U32 yomiid;
        U32 wordid;
        U16 lid;
        U16 rid;
        double wcost;
        double wcostfact;
        std::string Yomi;
        std::string Word;
        Candidate() {};
        static bool WHICH(Candidate& rLeft, Candidate& rRight) {
            return (rLeft.yomiid < rRight.yomiid);
        }
    };

    searcher();
    virtual ~searcher(void);
    virtual void Search(const U08 nPos, std::string& Key, std::string& Word, const Dictionary& aWordDictionary, const Dictionary& aSuffixDictionary, std::vector<searcher::Candidate>& candidate);

protected:

    virtual bool _searchForYomi(const Dictionary & aDictionary, U08* out, const U08 nPos, std::string& Key, std::string& Word, int inindex, int outindex, U32 bit_index, double wFactor, std::vector<searcher::Candidate>& candidate);
    virtual bool _searchForMida(const Dictionary & aDictionary, U08* out, const U08 nPos, std::string& Key, std::string& Word, int inindex, int outindex, U32 bit_index, double wFactor, std::vector<searcher::Candidate>& candidate);

    virtual double _IsMatch(const U08 a, const U08 b);
};
