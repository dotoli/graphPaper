
public class GraphElement {
	
	private int _id;
	private boolean _selected;
	private String _name;
	private Pos _pos;
	
	public int id()
	{
		return _id;
	}
	
	public void id(int i)
	{
		_id = i;
	}

	public boolean selected()
	{
		return _selected;
	}
	
	public void selected(boolean b)
	{
		_selected = b;
	}
	
	public String name()
	{
		return _name;
	}
	
	public void name(String s)
	{
		_name = s;
	}
	
	public Pos pos()
	{
		return _pos;
	}
	
	public void pos(Pos p)
	{
		_pos = new Pos(p);
	}
	
	public float x()
	{
		return pos().x();
	}
	
	public float y()
	{
		return pos().y();
	}
	
	public void x(float xx)
	{
		pos().x(xx);
	}
	
	public void y(float yy)
	{
		pos().y(yy);
	}
}
