#include "searcher.h"
#include "Dictionary.h"

searcher::searcher()
{
}

searcher::~searcher(void)
{

}

void searcher::Search(const U08 nPos, std::string& Key, std::string& Word, const Dictionary& aWordDictionary, const Dictionary& aSuffixDictionary, std::vector<searcher::Candidate>& candidate)
{
    U08 OUT[128] = { 0, };

    if (Key.length() > 0) {
        _searchForYomi(aWordDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
        if (candidate.size() == 0) {
            Word = "";
            _searchForMida(aSuffixDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
            if (Key.length() > 0) {
                _searchForYomi(aSuffixDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
            }
        }
    }
    else {
        _searchForMida(aWordDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
        if (Key.length() > 0) {
            _searchForYomi(aWordDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
            if (candidate.size() == 0) {
                Word = "";
                _searchForMida(aWordDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
                _searchForMida(aSuffixDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
                if (Key.length() > 0) {
                    _searchForYomi(aWordDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
                    _searchForYomi(aSuffixDictionary, OUT, nPos, Key, Word, 0, 0, loudstrie::kRootIndex, 1.0, candidate);
                }
            }
        }
    }
}

bool searcher::_searchForYomi(const Dictionary & aDictionary, U08* out, const U08 nPos, std::string& Key, std::string& Word, int inindex, int outindex, U32 bit_index, double wFactor, std::vector<searcher::Candidate>& candidate)
{
    const loudstrie& aYomiloudstrie = aDictionary.GetYomiTrie();
    const loudstrie& aWordloudstrie = aDictionary.GetWordTrie();

    const bitvectorbasedarray& aYomiToWordBitArray = aDictionary.GetYomiInfoBitArray();

    int nkeylen = Key.length();

    if (nkeylen + 4 <= inindex) return false;

    int _bit_index = bit_index, _child_node_id = aYomiloudstrie.GetChildNodeId(_bit_index) - 1;

    do {

        const U08 character = aYomiloudstrie.GetEdgeChar(_child_node_id);

        const U08 keychar = inindex < nkeylen ? Key.c_str()[inindex] : 0xFF;

        double wfactor = _IsMatch(keychar, character);

        //      if ((_IsMatch(keychar, character) && wfactor <= 1.0)) {
        if ((_IsMatch(keychar, character) && wfactor <= 1.0) || (inindex >= nkeylen && inindex < nkeylen + 6 && nPos == 0)) {

            if (aYomiloudstrie.IsTerminalNode(_child_node_id)) {

                out[outindex] = character;
                out[outindex + 1] = 0;

                U32 wYomiID = aYomiloudstrie.GetNodeNumber(_child_node_id);
                U32 yomilen = outindex + 1;

                U32 wLength;
                U08 uStep;

                const U08 * pData = aYomiToWordBitArray.Get(wYomiID, wLength, uStep);

                for (U32 nPos = 0; nPos < wLength; nPos += uStep) {

                    const U16 wCost = (pData[nPos + 0] << 8) + pData[nPos + 1];
                    const U16 wLeft = ((pData[nPos + 4] & 0xF0) << 4) + pData[nPos + 2];
                    const U16 wRight = ((pData[nPos + 4] & 0x0F) << 8) + pData[nPos + 3];
                    const U32 wWordID = (pData[nPos + 7] << 16) + (pData[nPos + 6] << 8) + pData[nPos + 5];

                    searcher::Candidate res;

                    double wcost = (wCost * 1.0);

                    if (wcost == 0) {
                        wcost = 1.0;
                    }

                    if (inindex > nkeylen) {
                        wcost /= 5;
                        //if (wLength > uStep * 4) continue;
                    }

                    res.yomiid = wYomiID;
                    res.wordid = wWordID;
                    res.lid = wLeft;
                    res.rid = wRight;
                    res.Word = "";//std::string("");
                    res.Yomi = std::string((char*)out);

                    res.wcost = wcost;
                    res.wcostfact = wfactor;

                    U08 len = 0; const char* pWord = (const char*)(aDictionary.GetWordString(wWordID, len));

                    if (Word.length() == 0) {
                        res.Word = std::string((char*)pWord);
                        candidate.push_back(res);
                    }
                    else {
                        bool bMatch = true;
                        int nLen = Word.length();
                        for (int n = 0; n < nLen; n++) {
                            if (Word.c_str()[n] != pWord[n]) {
                                bMatch = false;
                                break;
                            }
                        }
                        if (nLen < len && bMatch == true && wLeft != wRight) {
                            res.Word = std::string((char*)pWord + nLen);
                            candidate.push_back(res);
                        }
                    }
                }
            }

            out[outindex] = character;

            const int child_index = aYomiloudstrie.GetFirstEdgeBitIndex(_child_node_id) - 1;

            _searchForYomi(aDictionary, out, nPos, Key, Word, inindex + 1, outindex + 1, child_index, wfactor, candidate);
        }

        ++_bit_index;
        ++_child_node_id;
    } while (aYomiloudstrie.IsEdgeBit(_bit_index));

    return true;
}

bool searcher::_searchForMida(const Dictionary & aDictionary, U08* out, const U08 nPos, std::string& Key, std::string& Word, int inindex, int outindex, U32 bit_index, double wFactor, std::vector<searcher::Candidate>& candidate)
{
    const loudstrie& aYomiloudstrie = aDictionary.GetYomiTrie();
    const loudstrie& aWordloudstrie = aDictionary.GetWordTrie();

    const bitvectorbasedarray& aYomiToWordBitArray = aDictionary.GetYomiInfoBitArray();
    const bitvectorbasedarray& aMidaInfoBitArray = aDictionary.GetMidaInfoBitArray();

    int nwordlen = Word.length();

    int _bit_index = bit_index, _child_node_id = aWordloudstrie.GetChildNodeId(_bit_index) - 1;

    do {

        const U08 character = aWordloudstrie.GetEdgeChar(_child_node_id);

        const U08 c = (const U08)inindex < nwordlen ? (Word.c_str()[inindex]) : 0xFF;

        if (c == character || c == 0xFF) {

            if (aWordloudstrie.IsTerminalNode(_child_node_id)) {

                out[outindex] = character;
                out[outindex + 1] = 0;

                U32 wMidaID = aWordloudstrie.GetNodeNumber(_child_node_id);
                U32 wMidaLen = outindex + 1;

                U32 wMidaInfoLength = 0;
                U08 uMidaInfoStep = 0;

                const U08 * pMidaData = aMidaInfoBitArray.Get(wMidaID, wMidaInfoLength, uMidaInfoStep);
                for (U32 nMidaPos = 0; nMidaPos < wMidaInfoLength; nMidaPos += uMidaInfoStep) {

                    const U32 wYomiID = (pMidaData[nMidaPos + 2] << 16) + (pMidaData[nMidaPos + 1] << 8) + pMidaData[nMidaPos + 0];
                    U08 len = 0; const char* pYomi = (const char*)(aDictionary.GetYomiString(wYomiID, len));

                    Key.clear();
                    Key.append(pYomi);

                    return true;
                }
            }

            out[outindex] = character;

            const int child_index = aWordloudstrie.GetFirstEdgeBitIndex(_child_node_id) - 1;

            _searchForMida(aDictionary, out, nPos, Key, Word, inindex + 1, outindex + 1, child_index, 1, candidate);
        }

        ++_bit_index;
        ++_child_node_id;
    } while (aWordloudstrie.IsEdgeBit(_bit_index));

    return true;
}

double searcher::_IsMatch(const U08 a, const U08 b)
{
    if (a == b) {

        return 1.0;
    }

    double wfactor = 5.0;

    switch (a) {
    case  1: case  2: case  3: case  4: case  5:
    case  6: case  7: case  8: case  9: case 10:
        switch (b) {
        case  1: case  2: case  3: case  4: case  5:
        case  6: case  7: case  8: case  9: case 10:
            wfactor = 3.5;
            break;
        }
        break;
    case 11: case 12: case 13: case 14: case 15:
    case 16: case 17: case 18: case 19: case 20:
        switch (b) {
        case 11: case 12: case 13: case 14: case 15:
        case 16: case 17: case 18: case 19: case 20:
            wfactor = 3.5;
            break;
        }
        break;
    case 21: case 22: case 23: case 24: case 25:
    case 26: case 27: case 28: case 29: case 30:
        switch (b) {
        case 21: case 22: case 23: case 24: case 25:
        case 26: case 27: case 28: case 29: case 30:
            wfactor = 3.5;
            break;
        }
        break;
    case 31: case 32: case 33: case 34: case 35:
    case 36: case 37: case 38: case 39: case 40: case 41:
        switch (b) {
        case 31: case 32: case 33: case 34: case 35:
        case 36: case 37: case 38: case 39: case 40: case 41:
            wfactor = 3.5;
            break;
        }
        break;
    case 42: case 43: case 44: case 45: case 46:
        switch (b) {
        case 42: case 43: case 44: case 45: case 46:
            wfactor = 3.5;
            break;
        }
        break;
    case 47: case 48: case 49: case 50: case 51:
    case 52: case 53: case 54: case 55: case 56:
    case 57: case 58: case 59: case 60: case 61:
        switch (b) {
        case 47: case 48: case 49: case 50: case 51:
        case 52: case 53: case 54: case 55: case 56:
        case 57: case 58: case 59: case 60: case 61:
            wfactor = 3.5;
            break;
        }
        break;
    case 62: case 63: case 64: case 65: case 66:
        switch (b) {
        case 62: case 63: case 64: case 65: case 66:
            wfactor = 3.5;
            break;
        }
        break;
    case 67: case 68: case 69: case 70: case 71: case 72:
        switch (b) {
        case 67: case 68: case 69: case 70: case 71: case 72:
            wfactor = 3.5;
            break;
        }
        break;

    case 73: case 74: case 75: case 76: case 77:
        switch (b) {
        case 73: case 74: case 75: case 76: case 77:
            wfactor = 3.5;
            break;
        }
        break;
    case 78: case 79: case 80: case 81: case 82: case 83:
        switch (b) {
        case 78: case 79: case 80: case 81: case 82: case 83:
            wfactor = 3.5;
            break;
        }
        break;
    }

    return wfactor;
}
