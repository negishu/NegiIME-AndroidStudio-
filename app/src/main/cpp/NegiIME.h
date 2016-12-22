#ifndef _NEGIIME_
class Converter;
class NegiIME {

public:

	NegiIME();
	~NegiIME();

	const int ProcessData(const char* pInBuf, const int nInLen, char* pOutBuf, const int nOutLen);

	Converter* _pConverter;
};

#endif
