import java.util.LinkedList;


public class Cell extends GraphElement{
	
	private static int cellCount;
	
	//private static float PI =  3.14159265;
	private static final float _cellRadius = 1;
	
	
	private boolean _isArrowNode;
	private LinkedList<Port> _ports;
	private float[] _box = new float[4];   //   0=up   1=right   2=down    4=left
	private Graph _graph;
	private Cell _subGraph; // the arrownode it belongs to
	
	
	public Cell(String s, Pos p)
	{
		name(s);
		pos(p);
		
		_ports = new LinkedList<Port>();
		_subGraph = null;
		
		id(cellCount);
		cellCount++;
	}
	
	public Cell(Pos p)
	{
		this("",p);
	}
	
	public Cell(float x, float y)
	{
		this(new Pos(x,y));
	}
	
	public Cell(String s, float x, float y)
	{
		this(s,new Pos(x,y));
	}
	
	public boolean isArrowNode()
	{
		return _isArrowNode;
	}
	
	public void isArrowNode(boolean b)
	{
		_isArrowNode = b;
	}
	
	public int portSize()
	{
		return _ports.size();
	}
	
	public LinkedList<Port> port()
	{
		return _ports;
	}
	
	public Port port(int i)
	{
		return _ports.get(i);
	}
	
	public void addPort()
	{
		
		_ports.add(new Port("",this));
		updatePortPositions();
	}
	
	public void addPort(Port p)
	{
		
		_ports.add(new Port(p.name(),this));
		updatePortPositions();
	}
	
	public void removePort(int i)
	{
		
		//Remove any edge that may have this as a port.
		if(_ports.get(i).edge()!=null)
		{
			Port p1 = _ports.get(i).edge().port(1);
			Port p2 = _ports.get(i).edge().port(2);
			Edge e = _ports.get(i).edge().port(1).edge();
			
			p1.edge(null);
			p2.edge(null);
			e.port(1,null);
			e.port(2,null);
			
		}
		
		_ports.remove(i);
		updatePortPositions();
	}
	
	public void removePort(Port p)
	{
		for(int i = 0 ; i < _ports.size() ; i++)
		{
			if(p == _ports.get(i))
			{
				_ports.remove(i);
				i--;
			}
		}
	}
	
	public static float cellRadius()
	{
		return _cellRadius;
	}
	

	
	public void updatePortPositions()
	{
		
		for(int i = 0 ; i < _ports.size() ; i++)
		{

			Pos p = new Pos((float)Math.sin((360*i/_ports.size())*Math.PI/180)*cellRadius(),(float)Math.cos((360*i/_ports.size())*Math.PI/180)*cellRadius());

			Pos p2 = new Pos((float)Math.sin((360*i/_ports.size())*Math.PI/180)*(float)(cellRadius()+0.5),(float)Math.cos((360*i/_ports.size())*Math.PI/180)*(float)(cellRadius()+0.5));

			_ports.get(i).pos(p);
			_ports.get(i).labelPos(p2);

			//delete p;
			//delete p2;
		}
		
	}
	
	public float up()
	{
		return _box[0];
	}
	
	public float right()
	{
		return _box[1];
	}
	
	public float down()
	{
		return _box[2];
	}
	
	public float left()
	{
		return _box[3];
	}
	
	public void up(float f)
	{
		_box[0] = f;
	}
	
	public void right(float f)
	{
		_box[1] = f;
	}
	
	public void down(float f)
	{
		_box[2] = f;
	}
	
	public void left(float f)
	{
		_box[3] = f;
	}
	
	public void setupArrowNode()
	{
		_graph = new Graph();
		port().clear();
		up(4);
		right(4);
		down(-4);
		left(-4);
	}
	
	public void updateArrowCellBox()
	{
		Cell tempCell;
		
		up(0);
		right(0);
		down(0);
		left(0);
		
		for(int i = 0 ; i < graph().cellSize() ; i++)
		{
			tempCell = graph().cell(i);
			
			if(tempCell.y()-y()+2>up())
				up(tempCell.y()-y()+2);
			
			if(tempCell.y()-y()-2<down())
				down(tempCell.y()-y()-2);
			
			if(tempCell.x()-x()+2>right())
				right(tempCell.x()-x()+2);
			
			if(tempCell.x()-x()-2<left())
				left(tempCell.x()-x()-2);
			
		}
		
		if(up()<4)
			up(4);
		if(right()<4)
			right(4);
		if(down()>-4)
			down(-4);
		if(left()>-4)
			left(-4);
		
	}
	
	public void updateArrowCellBox2()
	{
		//System.out.println("Entered Box update");
		Cell tempCell = graph().cell(graph().cellSize()-1);
		
		//System.out.println("1: " + tempCell.x() + "   " + tempCell.y());
		//System.out.println("2: " + x() + "   " + y());
		//System.out.println("y: " + (tempCell.y()-y()));
		
		if(tempCell.y()-y()+2>up())
			up(tempCell.y()-y()+2);
		
		if(tempCell.y()-y()-2<down())
			down(tempCell.y()-y()-2);
		
		if(tempCell.x()-x()+2>right())
			right(tempCell.x()-x()+2);
		
		if(tempCell.x()-x()-2<left())
			left(tempCell.x()-x()-2);
		
	}
	
	public Graph graph()
	{
		return _graph;
	}
	
	public Cell subGraph()
	{
		return _subGraph;
	}
	
	public void subGraph(Cell c)
	{
		_subGraph = c;
	}
	
	public boolean lhs()
	{
		if(x()-subGraph().x()>0)
			return false;
		else
			return true;
	}
	

}
