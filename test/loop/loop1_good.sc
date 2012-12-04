void main()
{
	int i;
	int n;
	int i1;
	int n1;
	
	assume(n >= 0 && n <= 10 && i >= 0);
	
	i1 = i;
	n1 = n;
	
	while(n != 0)
	bound(10)
	{
		i1 = 2*i1;
		n = n-1;
	}
	
	assert(i1 == (i << n1));
	
	
	
	int a;
	int b;
	int a1;
	assume(a <= 100 && b >= 5 && a >= b && a1 == a);
	
	
	int div;
	div = 0;
	while(a > b)
	bound(20)
	{
		a = a-b;
		div = div + 1;
	}
	
	assert(div*b + a == a1);

}

