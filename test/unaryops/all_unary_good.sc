void main(int z)
{
	assert(-(-z) == +z);
	assert(~(~z) == z);
	assert((!z && z) == 0);
}