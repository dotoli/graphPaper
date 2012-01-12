import java.util.LinkedList;


public class Graph {

	private String _name;
	private LinkedList<Cell> _cells;
	private LinkedList<Edge> _edges;


	public Graph()
	{
		_cells = new LinkedList<Cell>();
		_edges = new LinkedList<Edge>();
	}

	public int cellSize()
	{
		return _cells.size();
	}

	public Cell cell(int i)
	{
		return _cells.get(i);
	}

	public void addCell(Cell c)
	{
		_cells.add(c);
	}

	public void addCell(String s, Pos p)
	{
		addCell(new Cell(s,p));
	}

	public void addCell(String s, float x, float y)
	{
		addCell(new Cell(s,x,y));
	}

	public void removeCell(Cell c)
	{
		for(int i = 0 ; i < c.portSize() ; i++)
		{
			if(c.port(i).edge()!=null)
			{
				Edge e = c.port(i).edge();
				Port p1 = e.port(1);
				Port p2 = e.port(2);

				p1.edge(null);
				p2.edge(null);

				e.port(1, null);
				e.port(2,null);


			}
			c.removePort(i);
		}

		_cells.remove(c);
	}

	public int edgeSize()
	{
		return _edges.size();
	}

	public Edge edge(int i)
	{
		return _edges.get(i);
	}

	public void addEdge(Port p1, Port p2)
	{
		Edge e = new Edge(p1,p2);
		p1.edge(e);
		p2.edge(e);
		_edges.add(e);
	}

	public void removeEdge(Edge e)
	{
		if(e.port(1)!=null)
			e.port(1).edge(null);
		if(e.port(2)!=null)
			e.port(2).edge(null);
		_edges.remove(e);
	}


	public String name()
	{
		return _name;
	}

	public void name(String s)
	{
		_name = s;
	}


}
