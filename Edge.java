
public class Edge extends GraphElement{
	
	private static int edgeCount;

	private Port _p1,_p2;
	
	public Edge(Port p1, Port p2)
	{
		port(1,p1);
		port(2,p2);
		
		id(edgeCount);
		edgeCount++;
	}

	public Port port(int i)
	{
		if(i==1)
			return _p1;
		else
			return _p2;
	}

	public void port(int i, Port p)
	{
		if(i==1)
			_p1 = p;
		else
			_p2 = p;
	}



}
