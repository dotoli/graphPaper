
public class Pos {
	private float _x,_y;
	
	
	public Pos(float xx, float yy)
	{
		this.x(xx);
		this.y(yy);
	}
	
	public Pos()
	{
		this(0,0);
	}
	
	public Pos(Pos p)
	{
		this(p.x(),p.y());
	}
	
	public float x()
	{
		return _x;
	}
	
	public float y()
	{
		return _y;
	}
	
	public void x(float x)
	{
		_x=x;
	}
	
	public void y(float y)
	{
		_y=y;
	}
	
}
