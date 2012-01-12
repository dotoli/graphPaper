
public class Port extends GraphElement{
	
	private static int portCount = 0;
	private static final float _portRadius = 0.2f;
	
	
	private Pos _labelPos;
	private Cell _cell;
	private Edge _edge;
	
	
	
	public Port(String s, Cell c)
	{
		name(s);
		cell(c);
		
		id(portCount);
		portCount++;
	}
	
	public static float portRadius()
	{
		return _portRadius;
	}
	

	
	public Pos labelPos()
	{
		return _labelPos;
	}
	
	public void labelPos(Pos p)
	{
		_labelPos = new Pos(p);
	}
	
	public Cell cell()
	{
		return _cell;
	}
	
	public void cell(Cell c)
	{
		_cell = c;
	}
	
	public Edge edge()
	{
		return _edge;
	}
	
	public void edge(Edge e)
	{
		_edge = e;
	}
	

	

}
