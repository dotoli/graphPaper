import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.BufferUtils;

import java.awt.Font;


import java.nio.*;
import java.util.LinkedList;

public class GraphPaper  {


	Font font;
	TrueTypeFont trueTypeFont;
	float fontScale = 0.015f;

	float screenWidth = 1024;
	float screenHeight = 768;

	float[] camera;
	float[] cameraGo;
	float[] cameraV;
	float[] vIntersect;
	float[] vIntersectOld;
	float[] mouse3d;
	float[] mouse3dOld;

	int[] mouse2d;
	int[] mouse2dOld;

	boolean mouseMoving;
	//boolean dragging;
	boolean mouseDown;
	boolean mouseDownOld;
	int buttonDown;

	boolean typing = false;


	Graph graph;

	public LinkedList<Cell> selectedCells;
	public Port selectedPort;
	public LinkedList<Pos> drawing;


	/** time at last frame */
	long lastFrame;

	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;




	public void start() throws LWJGLException {
		try {
			Display.setDisplayMode(new DisplayMode((int)screenWidth, (int)screenHeight));
			Display.create();

		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		initVars();
		initGraph();
		initGL(); // init OpenGL
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer

		while (!Display.isCloseRequested()) {
			int delta = getDelta();

			update(delta);
			renderGL();

			Display.update();
			Display.sync(60); // cap fps to 60fps
		}

		Display.destroy();
	}




	public void update(int delta) {
		// rotate quad



		mouse2dOld[0] = mouse2d[0];
		mouse2dOld[1] = mouse2d[1];

		mouse2d[0] = Mouse.getX();
		mouse2d[1] = Mouse.getY();

		mouse3dOld[0] = mouse3d[0];
		mouse3dOld[1] = mouse3d[1];
		get3dMousePosition(mouse2d[0],mouse2d[1]);
		mouse3dOld[0] = mouse3d[0] - mouse3dOld[0];
		mouse3dOld[1] = mouse3d[1] - mouse3dOld[1];


		//System.out.println(mouse3dOld[0] + "   :   " + mouse3dOld[1]);

		//System.out.println(dragging);


		if(mouse2d[0]-mouse2dOld[0]!=0 || mouse2d[1]-mouse2dOld[1]!=0)
			mouseMoving = true;
		else
			mouseMoving = false;

		mouseDownOld = mouseDown;

		while(Mouse.next())
		{
			if(Mouse.getEventButton()!=-1)
			{
				buttonDown = Mouse.getEventButton();

				mouseDown = Mouse.getEventButtonState();
			}
		}

		if(mouseDown && !mouseDownOld) // PRESSED
		{
			if(buttonDown==0) // BUTTON 1
			{
				//System.out.println("PRESSED1");
				//if(!dragging)
				getElemAtMouse();

			}
			else // BUTTON 2
			{
				//System.out.println("PRESSED2");
			}
		}

		if(!mouseDown && mouseDownOld) // RELEASED
		{
			if(buttonDown==0) // BUTTON 1
			{
				//System.out.println("RELEASED1");


			}
			else // BUTTON 2
			{
				processDrawing();
				drawing.clear();
				//System.out.println("RELEASED2");
			}
			//dragging = false;

		}

		if(mouseDown) // MOUSE IS DOWN
		{
			if(buttonDown==0) // BUTTON 1
			{
				//System.out.println("DOWN1");

				if(mouseMoving)
					//dragging = true;

					if(selectedCells.size()==0 && selectedPort == null)
					{
						cameraGo[0] = cameraGo[0] + (mouse2d[0]-mouse2dOld[0])*delta*-0.002f;
						cameraGo[1] = cameraGo[1] + (mouse2d[1]-mouse2dOld[1])*delta*-0.002f;
					}

				if(selectedCells.size()>0) // IF AT LEAST ONE CELL IS SELECTED
				{

					for(int i = 0 ; i < selectedCells.size() ; i++)
					{
						Cell tempCell = selectedCells.get(i);
						tempCell.x(tempCell.x()+mouse3dOld[0]);
						tempCell.y(tempCell.y()+mouse3dOld[1]);

						//if in a rule then update box of rule
						if(tempCell.subGraph()!=null)
							tempCell.subGraph().updateArrowCellBox();

						//if an arrow node then move all it's cells too
						if(tempCell.isArrowNode())
						{
							Cell subCell;

							for(int j = 0 ; j < tempCell.graph().cellSize() ; j++)
							{
								subCell = tempCell.graph().cell(j);

								//if not already in the selectedCells (to avoid "double" moving)
								if(!selectedCells.contains(subCell))
								{
									subCell.x(subCell.x()+mouse3dOld[0]);
									subCell.y(subCell.y()+mouse3dOld[1]);
								}
							}
						}
					}
				}

			}
			else // BUTTON 2
			{
				drawing.add(new Pos(mouse3d[0],mouse3d[1]));
			}
		}






		//System.out.println(mouse3d[0]+ " : " + mouse3d[1]);

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) cameraGo[0] -= 0.02f * delta;
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) cameraGo[0] += 0.02f * delta;

		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) cameraGo[1] += 0.02f * delta;
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) cameraGo[1] -= 0.02f * delta;

		while (Keyboard.next()) {


			if (Keyboard.getEventKeyState()) {

				//System.out.println(Keyboard.getEventCharacter());

				if(selectedPort!=null)
				{
					if(Keyboard.getEventKey() == Keyboard.KEY_BACK)
					{
						if(selectedPort.name().length()>0)
							selectedPort.name(selectedPort.name().substring(0, selectedPort.name().length()-1));
					}
					else if(Keyboard.getEventKey() == Keyboard.KEY_RETURN)
					{
						typing = false;
						selectedPort.selected(false);
						selectedPort = null;
					}
					else if(Keyboard.getEventKey() != Keyboard.KEY_RSHIFT && Keyboard.getEventKey() != Keyboard.KEY_LSHIFT)
					{
						selectedPort.name(selectedPort.name()+Keyboard.getEventCharacter());
					}

				}
				else if(selectedCells.size()==1)
				{
					if(Keyboard.getEventKey() == Keyboard.KEY_BACK)
					{
						if(selectedCells.get(0).name().length()>0 && !selectedCells.get(0).name().equals("=>"))
							selectedCells.get(0).name(selectedCells.get(0).name().substring(0, selectedCells.get(0).name().length()-1));
					}
					else if(Keyboard.getEventKey() == Keyboard.KEY_RETURN)
					{
						typing = false;

						if(selectedCells.get(0).portSize() == 0)
							copyPortsFromType(selectedCells.get(0));

						if(selectedCells.get(0).name().equals("=>"))
						{
							//check that we're not already IN a rule
							if(selectedCells.get(0).subGraph()==null)
							{
								selectedCells.get(0).isArrowNode(true);
								selectedCells.get(0).setupArrowNode();
							}
							else
							{
								selectedCells.get(0).name("");
							}
						}
						else
							selectedCells.get(0).isArrowNode(false);


						selectedCells.get(0).selected(false);
						selectedCells.clear();
					}
					else if(Keyboard.getEventKey() != Keyboard.KEY_RSHIFT && Keyboard.getEventKey() != Keyboard.KEY_LSHIFT)
					{
						if(!selectedCells.get(0).name().equals("=>"))
							selectedCells.get(0).name(selectedCells.get(0).name()+Keyboard.getEventCharacter());
					}

				}

				/*
				if (Keyboard.getEventKey() == Keyboard.KEY_A) {
					//System.out.println("A Key Pressed");
					graph.cell(0).addPort();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_S) {
					//System.out.println("S Key Pressed");
					graph.cell(0).removePort(graph.cell(0).portSize()-1);
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D) {
					getElemAtMouse();
				}
				 */


			} else {

				/*
				if (Keyboard.getEventKey() == Keyboard.KEY_A) {
					System.out.println("A Key Released");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_S) {
					System.out.println("S Key Released");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D) {
					//System.out.println("D Key Released");
				}

				 */
			}
		}




		updateFPS(); // update FPS Counter
	}

	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	public int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;

		return delta;
	}

	/**
	 * Get the accurate system time
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public void initGraph()
	{
		graph = new Graph();

		//graph.addCell("a",-2,-3);


		graph.addCell("=>",2,2);
		graph.cell(0).isArrowNode(true);
		graph.cell(0).setupArrowNode();

	}

	public void initVars()
	{

		font = new Font("Serif", Font.PLAIN, 32);
		trueTypeFont = new TrueTypeFont(font, true);
		glDisable(GL_TEXTURE_2D );

		camera = new float[3];
		cameraGo = new float[3];
		cameraV = new float[3];
		vIntersect = new float[3];
		vIntersectOld = new float[3];

		mouse3d = new float[2];
		mouse3dOld = new float[2];
		mouse2d = new int[2];
		mouse2dOld = new int[2];

		camera[0] = 0;
		camera[1] = 0;
		camera[2] = -4;

		cameraGo[0] = 0;
		cameraGo[1] = 0;
		cameraGo[2] = -30;

		cameraV[0] = 0;
		cameraV[1] = 0;
		cameraV[2] = 0;

		vIntersect[0] = 0;
		vIntersect[1] = 0;
		vIntersect[2] = 0;

		vIntersectOld[0] = 0;
		vIntersectOld[1] = 0;
		vIntersectOld[2] = 0;

		selectedCells = new LinkedList<Cell>();
		drawing = new LinkedList<Pos>();

	}

	public void initGL() throws LWJGLException {

		glEnable(GL_LINE_SMOOTH);

		glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		glClearDepth(1.0);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); 
		glShadeModel(GL_SMOOTH);

		glEnable(GL_COLOR_MATERIAL);
		//glEnable(GL11.GL_TEXTURE_2D);
		glDisable(GL11.GL_DITHER);

		//glDepthFunc(GL11.GL_LESS); // Depth function less or equal
		glEnable(GL11.GL_NORMALIZE); // calculated normals when scaling
		glEnable(GL11.GL_CULL_FACE); // prevent render of back surface   
		glEnable(GL11.GL_BLEND); // Enabled blending
		glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // selects blending method
		glEnable(GL11.GL_ALPHA_TEST); // allows alpha channels or transperancy
		glAlphaFunc(GL11.GL_GREATER, 0.1f); // sets aplha function
		glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); // High quality visuals
		glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST); //  Really Nice Perspective Calculations
		glShadeModel(GL11.GL_SMOOTH); // Enable Smooth Shading


		glViewport(0, 0, (int)screenWidth, (int)screenHeight);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluPerspective(45.0f, (float) screenWidth / (float) screenHeight, 1.0f, 100.0f);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		GL11.glDepthMask(true);	


	}

	public void renderGL() {

		// Clear The Screen And The Depth Buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();

		updateCamera();

		glTranslatef(-camera[0], -camera[1], camera[2]);

		// R,G,B,A Set The Color To Blue One Time Only
		glColor3f(1.0f, 1.0f, 1.0f);

		glDisable(GL_TEXTURE_2D);

		glPushMatrix();
		//gl.glTranslatef(0, 0, 5);
		glColor3f(1, 0, 0);
		glBegin(GL_QUADS);
		glVertex3f(0,0,0.1f);
		glVertex3f(0,1,0.1f);
		glVertex3f(1,1,0.1f);
		glVertex3f(1,0,0.1f);
		glEnd();
		glPopMatrix();


		glPushMatrix();
		//gl.glTranslatef(0, 0, 5);
		glColor3f(1, 0, 0);
		glBegin(GL_QUADS);
		glVertex3f(0,4,0);
		glVertex3f(0,5,0);
		glVertex3f(1,5,0);
		glVertex3f(1,4,0);
		glEnd();
		glPopMatrix();



		drawGrid();

		drawCellsAndPorts();
		updateEdges();
		drawDrawing();




	}

	public void drawString(float x, float y, String s)
	{
		glEnable(GL_TEXTURE_2D );
		glColor3f(0, 0, 0);
		trueTypeFont.drawString(x, y - (trueTypeFont.getHeight()*fontScale/2), s, fontScale,fontScale,TrueTypeFont.ALIGN_CENTER);
		glDisable(GL_TEXTURE_2D );
	}

	public static void main(String[] argv) throws LWJGLException {
		GraphPaper graphPaper = new GraphPaper();

		graphPaper.start();
	}


	public void updateCamera()
	{
		cameraGo[0] += cameraV[0];
		cameraGo[1] += cameraV[1];
		cameraGo[2] += cameraV[2];

		camera[0]=curveValue(cameraGo[0], camera[0], 10);
		camera[1]=curveValue(cameraGo[1], camera[1], 10);
		camera[2]=curveValue(cameraGo[2], camera[2], 10);


	}


	public void drawCircle(float x, float y, float z, float r, int step,float fill[],float line[])
	{

		glColor3f(fill[0],fill[1],fill[2]);
		glBegin(GL_POLYGON);

		for(int i = 0 ; i<360 ; i=i+step)
		{
			glVertex3f(x+r*(float)Math.cos(i*Math.PI/180),y+r*(float)Math.sin(i*Math.PI/180),z);
		}

		glEnd();


		glColor3f(line[0],line[1],line[2]);
		glBegin(GL_LINE_STRIP);

		for(int i = 0 ; i<=360 ; i=i+step)
		{
			glVertex3f(x+r*(float)Math.cos(i*Math.PI/180),y+r*(float)Math.sin(i*Math.PI/180),z);
		}



		glEnd();
	}

	public void drawLine(float x1, float y1, float x2, float y2, float z, boolean dotted,float col[])
	{

		glColor4f(col[0],col[1],col[2],col[3]);

		if(dotted)
			glBegin(GL_LINES);
		else
			glBegin(GL_LINES);

		glVertex3f(x1, y1, z);
		glVertex3f(x2, y2, z);

		glEnd();
	}

	public void drawRect(float u, float r, float d, float l, boolean dotted,float col[])
	{
		drawLine(l, u, r, u, 0.01f, false, col);
		drawLine(l, d, r, d, 0.01f, false, col);
		drawLine(l, u, l, d, 0.01f, false, col);
		drawLine(r, u, r, d, 0.01f, false, col);

	}

	public void drawDrawing()
	{
		float blue[] = {0,0,1,1};

		if(drawing.size()>1)
		{
			for(int i = 0 ; i < drawing.size()-1 ; i++)
			{
				drawLine(drawing.get(i).x(),drawing.get(i).y(),drawing.get(i+1).x(),drawing.get(i+1).y(),0.08f,false,blue);
			}
		}
	}

	public void drawCellsAndPorts()
	{



		float black[] = {0,0,0,1};
		float white[] = {1,1,1,1};
		float red[] = {1,0,0,1};

		float lx, ly;


		for(int i = 0 ; i < graph.cellSize() ; i++)
		{


			//draw the cell itself



			if(graph.cell(i).selected()==false)
				drawCircle(graph.cell(i).x(), graph.cell(i).y(),0.01f, Cell.cellRadius(), 10, white, black);
			else
				drawCircle(graph.cell(i).x(), graph.cell(i).y(),0.01f, Cell.cellRadius(), 10, white, red);


			drawString(graph.cell(i).x(), graph.cell(i).y(), graph.cell(i).name());

			if(graph.cell(i).isArrowNode())
			{
				//draw BOX
				drawRect(graph.cell(i).y()+graph.cell(i).up(), graph.cell(i).x()+graph.cell(i).right(), graph.cell(i).y()+graph.cell(i).down(), graph.cell(i).x()+graph.cell(i).left(), false, black);
				//draw cells in subgraph


			}


			//go through each port and draw it

			for(int j = 0 ; j < graph.cell(i).portSize() ; j++)
			{

				if(graph.cell(i).port(j).selected()==false)
					drawCircle(graph.cell(i).x() + graph.cell(i).port(j).x(), graph.cell(i).y() + graph.cell(i).port(j).y(),0.015f , Port.portRadius(), 15, white, black);
				else
					drawCircle(graph.cell(i).x() + graph.cell(i).port(j).x(), graph.cell(i).y() + graph.cell(i).port(j).y(),0.015f , Port.portRadius(), 15, white, red);


				drawString(graph.cell(i).x() + graph.cell(i).port(j).labelPos().x(), graph.cell(i).y() + graph.cell(i).port(j).labelPos().y(), graph.cell(i).port(j).name());



			}

		}



	}

	public void updateEdges()
	{

		// DRAWS THE EDGES IF THEY ARE VALID AND DELETES THEM IF THEY ARENT;

		float black[] = {0,0,0,1};
		float white[] = {1,1,1,1};

		float red[] = {1,0,0,1};

		for(int i = 0 ; i < graph.edgeSize() ; i++)
		{
			Port p1 = graph.edge(i).port(1);
			Port p2 = graph.edge(i).port(2);


			if(p1!=null && p2!=null)
			{
				//is this edge a rule connector?

				// is it in a subgraph
				if(p1.cell().subGraph()!=null)
				{
					//System.out.println("Subgraph not null!");
					//are the 2 cells on different sides of the arrow node?
					if(p1.cell().lhs()!=p2.cell().lhs())
					{
						drawLine(p1.cell().x() + p1.x(), p1.cell().y() + p1.y(), p2.cell().x() + p2.x(), p2.cell().y() + p2.y(),0.06f, true, red);
					}
					else
					{
						drawLine(p1.cell().x() + p1.x(), p1.cell().y() + p1.y(), p2.cell().x() + p2.x(), p2.cell().y() + p2.y(),0.06f, true, black);
					}
				}
				else
				{
					drawLine(p1.cell().x() + p1.x(), p1.cell().y() + p1.y(), p2.cell().x() + p2.x(), p2.cell().y() + p2.y(),0.06f, true, black);
				}
			}
			else
			{
				graph.removeEdge(graph.edge(i));
				i--;
			}


		}

	}

	public void drawGrid()
	{
		glColor4f(0.7f,0.7f,0.7f,1f);
		glBegin(GL_LINES);
		for(int x = (int)camera[0]-20 ; x < camera[0]+21 ; x++)
		{

			glVertex3f(x,-100,0);
			glVertex3f(x,100,0);

		}

		for(int x = (int)camera[1]-20 ; x < camera[1]+21 ; x++)
		{

			glVertex3f(-100,x,0);
			glVertex3f(100,x,0);

		}
		glEnd();

		glColor4f(0,0,1,1);
		//renderText(0, 0, 0.1, "(0,0)");
	}

	//public void drawText(String s, float x, float y, float z, float s = 1, float r = 0 , float g = 0 , float b = 0)
	//{

	//glPushMatrix();

	//glColor3f(r,g,b);

	//glTranslatef(x,y,z);

	//glScalef(s/FONTSCALE,s/FONTSCALE,s/FONTSCALE);

	//font[1]->Render(c);

	//glPopMatrix();

	//}

	public void get3dMousePosition(int mouseX, int mouseY)
	{
		/*

		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
		FloatBuffer projection = BufferUtils.createFloatBuffer(16);
		FloatBuffer winZ = BufferUtils.createFloatBuffer(1);
		float winX, winY;
		FloatBuffer position = BufferUtils.createFloatBuffer(3);

		GL11.glGetFloat( GL11.GL_MODELVIEW_MATRIX, modelview );
		GL11.glGetFloat( GL11.GL_PROJECTION_MATRIX, projection );
		GL11.glGetInteger( GL11.GL_VIEWPORT, viewport );

		winX = (float)mouseX;
		winY = (float)viewport.get(3) - (float)mouseY;



		GL11.glReadPixels(mouseX, (int)winY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, winZ);

		GLU.gluUnProject(winX, winY, winZ.get(), modelview, projection, viewport, position);


		mouse3d[0] = position.get(0);
		mouse3d[1] = position.get(1);


		float z = position.get(2);
		 */

		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
		FloatBuffer projection = BufferUtils.createFloatBuffer(16);
		FloatBuffer position = BufferUtils.createFloatBuffer(3);
		FloatBuffer position2 = BufferUtils.createFloatBuffer(3);

		int realy = 0;// GL y coord pos
		float[] wcoord = new float[4];
		float[] wcoord2 = new float[4];



		int x = mouseX;
		int y = mouseY;

		GL11.glGetFloat( GL11.GL_MODELVIEW_MATRIX, modelview );
		GL11.glGetFloat( GL11.GL_PROJECTION_MATRIX, projection );
		GL11.glGetInteger( GL11.GL_VIEWPORT, viewport );


		//realy = viewport.get(3) - y; //- (int) y - 1;

		realy = y;



		GLU.gluUnProject((float) x, (float) realy, 0.0f, //  replaced 0.0 by z
				modelview,
				projection,
				viewport,
				position);


		wcoord2[0]=position.get(0);
		wcoord2[1]=position.get(1);
		wcoord2[2]=position.get(2);


		GLU.gluUnProject((float) x, (float) realy, 1.0f, //
				modelview,
				projection,
				viewport,
				position2);

		wcoord[0]=position2.get(0);
		wcoord[1]=position2.get(1);
		wcoord[2]=position2.get(2);

		// Returns in (fX, fY) the location on the plane (P1,P2,P3) of the intersection with the ray (R1, R2)
		// First compute the axes
		float[] r1,r2,p1,p2,p3,v1,v2,v3;

		r1 = new float[3];
		r2 = new float[3];
		p1 = new float[3];
		p2 = new float[3];
		p3 = new float[3];
		v1= new float[3];
		v2 = new float[3];
		v3 = new float[3];


		r1[0] = wcoord[0];
		r1[1] = wcoord[1];
		r1[2] = wcoord[2];

		r2[0] = wcoord2[0];
		r2[1] = wcoord2[1];
		r2[2] = wcoord2[2];

		p1[0]=0;p1[1]=0;p1[2]=0;
		p2[0]=0;p2[1]=1;p2[2]=0;
		p3[0]=2;p3[1]=1;p3[2]=0;



		v1[0]=p2[0]-p1[0];// = P2 - P1;
		v1[1]=p2[1]-p1[1];
		v1[2]=p2[2]-p1[2];

		v2[0]=p3[0]-p1[0];// = P3 - P1;
		v2[1]=p3[1]-p1[1];
		v2[2]=p3[2]-p1[2];

		//cross product
		//x=(a.y*b.z)-(a.z*b.y);
		//y=(a.x*b.z)-(a.z*b.x);
		//z=(a.x*b.y)-(a.y*b.x);

		// = CrossProduct ( V1, V2);

		v3[0] = (v1[1]*v2[2]) - (v1[2]*v2[1]);
		v3[1] = (v1[0]*v2[2]) - (v1[2]*v2[0]);
		v3[2] = (v1[0]*v2[1]) - (v1[1]*v2[0]);


		// Project ray points R1 and R2 onto the axes of the plane. (This is equivalent to a rotation.)


		float[] r1p1 = new float[3];
		float[] r2p1 = new float[3];

		r1p1[0] = r1[0] - p1[0];
		r1p1[1] = r1[1] - p1[1];
		r1p1[2] = r1[2] - p1[2];

		r2p1[0] = r2[0] - p1[0];
		r2p1[1] = r2[1] - p1[1];
		r2p1[2] = r2[2] - p1[2];

		float[] vRotRay1 = new float[3];
		float[] vRotRay2 = new float[3];

		//dot prod  (x*a.x)+(y*a.y)+(z*a.z);
		vRotRay1[0] = v1[0]*r1p1[0] + v1[1]*r1p1[1] + v1[2]*r1p1[2];
		vRotRay1[1] = v2[0]*r1p1[0] + v2[1]*r1p1[1] + v2[2]*r1p1[2];
		vRotRay1[2] = v3[0]*r1p1[0] + v3[1]*r1p1[1] + v3[2]*r1p1[2];

		vRotRay2[0] = v1[0]*r2p1[0] + v1[1]*r2p1[1] + v1[2]*r2p1[2];
		vRotRay2[1] = v2[0]*r2p1[0] + v2[1]*r2p1[1] + v2[2]*r2p1[2];
		vRotRay2[2] = v3[0]*r2p1[0] + v3[1]*r2p1[1] + v3[2]*r2p1[2];

		// Return now if ray will never intersect plane (they're parallel)
		if(vRotRay1[2] == vRotRay2[2])
			System.out.println("NEVER INTERSECT");




		// Find 2D plane coordinates (fX, fY) that the ray interesects with
		float fPercent = vRotRay1[2] / (vRotRay2[2]-vRotRay1[2]);


		vIntersectOld[0] = vIntersect[0];
		vIntersectOld[1] = vIntersect[1];
		vIntersectOld[2] = vIntersect[2];

		// Note that to find the 3D point on the world-space ray use this
		//= R1 + (R1-R2) * fPercent;
		vIntersect[0] = r1[0]+(r1[0]-r2[0])*fPercent;
		vIntersect[1] = r1[1]+(r1[1]-r2[1])*fPercent;
		vIntersect[2] = r1[2]+(r1[2]-r2[2])*fPercent;


		mouse3d[0] = vIntersect[0];
		mouse3d[1] = vIntersect[1];
		//cout << "Intersect at: " << vIntersect[0] << "," << vIntersect[1] << "," << vIntersect[2] << endl;


		//vIntersect.x = vIntersect.x;
		//vIntersect.y = vIntersect.y;

		// UNCOMMENT THESE TO GET 3D WORLD COORD OF MOUSE on -10z PLANE
		//System.out.println("HITTING at:");
		//System.out.println(""+vIntersect.x+":"+vIntersect.y+":"+vIntersect.z);
		//System.out.println(camera.z);
		//System.out.println(""+fX+":"+fY);



		//System.out.println(mouse3d[0]+" : " + mouse3d[1] + " : " + vIntersect[2]);



	}

	public void getElemAtMouse()
	{

		System.out.println("Searching");

		LinkedList<Cell> selCell = new LinkedList<Cell>();
		boolean cellAlreadySelected = false;

		for(int i = 0 ; i < selectedCells.size(); i++)
		{
			selCell.add(selectedCells.get(i));
		}


		Cell tempCell = null;
		Port tempPort = null;
		boolean portFound = false;

		selectedCells.clear();
		selectedPort = null;





		//GO THROUGH EACH CELL CHECKING IF ONE OF THEIR PORTS ARE HIGHLIGHTED, THEN CHECK THE CELL ITSELF
		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);
			tempCell.selected(false);

			for(int j = 0 ; j < tempCell.portSize() ; j++)
			{
				tempPort = tempCell.port(j);
				tempPort.selected(false);

				if(distance(tempCell.x() + tempPort.x(), tempCell.y() + tempPort.y(), mouse3d[0], mouse3d[1]) <= Port.portRadius()*Port.portRadius())
				{
					tempPort.selected(true);
					System.out.println("FOUND PORT");
					selectedPort = tempPort;
					portFound = true;
					break;
				}

			}

			if(portFound==false)
			{
				if(distance(tempCell.x(), tempCell.y(),mouse3d[0], mouse3d[1])<=Cell.cellRadius()*Cell.cellRadius())
				{
					tempCell.selected(true);
					System.out.println("FOUND CELL");
					selectedCells.add(tempCell);

					if(selCell.contains(tempCell))
						cellAlreadySelected = true;

				}
			}

		}

		if(cellAlreadySelected)
		{
			selectedCells.clear();

			for(int i = 0 ; i < selCell.size() ; i++)
			{
				selectedCells.add(selCell.get(i));
				selCell.get(i).selected(true);
			}

		}


		//CHECK ABOUT RULE TITLES SO WE CAN CHANGE THE NAMES

	}

	public boolean circleWasDrawn()
	{


		if(distance(drawing.get(0).x(),drawing.get(0).y(),drawing.get(drawing.size()-1).x(),drawing.get(drawing.size()-1).y())<1)
			return true;
		else
			return false;
	}

	public Cell inCell(float x, float y)
	{
		Cell tempCell = null;

		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);

			if(distance(tempCell.x(),tempCell.y(),x,y)<Cell.cellRadius()*Cell.cellRadius())
			{
				return tempCell;
			}

		}

		return null;
	}

	public Cell inRuleSubgraph(float x, float y)
	{
		Cell tempCell;

		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);

			if(tempCell.isArrowNode())
			{

				if(x<tempCell.x()+tempCell.right() && x>tempCell.x()+tempCell.left() && y<tempCell.y()+tempCell.up() && y>tempCell.y()+tempCell.down())
					return tempCell;
			}
		}

		return null;
	}

	public Port inPort(float x,float y)
	{
		Cell tempCell = null;
		Port tempPort = null;

		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);



			for(int j = 0 ; j < tempCell.portSize(); j++)
			{

				tempPort = tempCell.port(j);

				if(distance(tempCell.x()+tempPort.x(),tempCell.y()+tempPort.y(),x,y)<Port.portRadius()*Port.portRadius())
				{

					return tempPort;
				}
			}

		}

		return null;
	}

	public Cell crossCell()
	{

		Cell tempCell;

		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);

			for(int j = 0 ; j < drawing.size() ; j++)
			{
				if(distance(tempCell.x(),tempCell.y(),drawing.get(j).x(),drawing.get(j).y())<=Cell.cellRadius()*Cell.cellRadius())
				{
					return tempCell;
				}
			}
		}

		return null;
	}

	public Port crossPort()
	{

		Cell tempCell;
		Port tempPort;

		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);

			for(int p = 0 ; p < tempCell.portSize() ; p++)
			{
				tempPort = tempCell.port(p);

				for(int j = 0 ; j < drawing.size() ; j++)
				{
					if(distance(tempCell.x()+tempPort.x(),tempCell.y()+tempPort.y(),drawing.get(j).x(),drawing.get(j).y())<=Port.portRadius()*Port.portRadius())
					{
						return tempPort;
					}
				}
			}
		}

		return null;
	}

	public LinkedList<Edge> crossEdges()
	{
		for(int i = 0 ; i < graph.edgeSize() ; i++)
		{
			Port p1 = graph.edge(i).port(1);
			Port p2 = graph.edge(i).port(2);

			if(p1==null || p2==null)
			{
				graph.removeEdge(graph.edge(i));
				i--;
			}
		}


		LinkedList<Edge> edges = new LinkedList<Edge>();

		for(int i = 0 ; i < graph.edgeSize() ; i++)
		{
			Edge tempEdge = graph.edge(i);

			if(lineInter(new Pos(tempEdge.port(1).x()+tempEdge.port(1).cell().x(),tempEdge.port(1).y()+tempEdge.port(1).cell().y()), new Pos(tempEdge.port(2).x()+tempEdge.port(2).cell().x(),tempEdge.port(2).y()+tempEdge.port(2).cell().y()), drawing.getFirst(), drawing.getLast()))
			{

				edges.add(tempEdge);
			}

		}



		return edges;
	}

	void processDrawing()
	{
		// WAS A CIRCLE DRAWN?
		if(circleWasDrawn())
		{
			System.out.println("Circle was drawn");

			// Is the circle empty?

			selectedCells.clear();

			for(int i = 0 ; i < graph.cellSize() ; i++)
			{
				Cell tempCell = graph.cell(i);

				if(insidePolygon(drawing, tempCell.pos()))
				{
					tempCell.selected(true);
					selectedCells.add(tempCell);
				}

			}	


			if(selectedCells.size()==0)
			{
				//was the cell created in the main graph or a rule subgraph?
				Cell arrowCell = inRuleSubgraph(drawing.get(0).x(),drawing.get(0).y());

				if(arrowCell==null)
				{
					System.out.println("Added to graph");
					graph.addCell("",drawing.get(0).x(),drawing.get(0).y());
				}
				else
				{
					System.out.println("Added to graph and subgraph");
					arrowCell.graph().addCell("",drawing.get(0).x(),drawing.get(0).y());
					graph.addCell(arrowCell.graph().cell(arrowCell.graph().cellSize()-1));
					arrowCell.graph().cell(arrowCell.graph().cellSize()-1).subGraph(arrowCell);
					arrowCell.updateArrowCellBox();
				}


			}
		}
		else
		{


			//DID LINE START IN A PORT?
			Port tempPort = inPort(drawing.get(0).x(),drawing.get(0).y());


			//DID LINE START IN A CELL?
			Cell tempCell = inCell(drawing.get(0).x(),drawing.get(0).y());



			if(tempPort!=null)
			{
				// Does the line end on a port? THEN CREATE AN EDGE!

				Port tempPort2 = inPort(drawing.get(drawing.size()-1).x(),drawing.get(drawing.size()-1).y());

				if(tempPort2!=null)
				{
					if(tempPort.cell().subGraph() == tempPort2.cell().subGraph())
					{
						if(tempPort.edge()==null && tempPort2.edge()==null)
							graph.addEdge(tempPort,tempPort2);
					}
				}



			}
			else if(tempCell!=null)
			{
				//DID YOU FINISH OUTSIDE OF THE STARTING CIRCLE
				if(distance(tempCell.x(),tempCell.y(),drawing.get(drawing.size()-1).x(),drawing.get(drawing.size()-1).y())>Cell.cellRadius()*Cell.cellRadius())
				{
					//CREATE A NEW PORT
					if(!tempCell.isArrowNode())
					{
					tempCell.addPort();
					updateCellTypePortAdd(tempCell, tempCell.port().getLast());
					}
				}
			}
			else //NO IT DIDNT SO CHECK FOR A DELETE
			{
				//options are: delete cell or delete port or delete edge

				System.out.println("Started on canvas");

				//check if it ends inside a cell
				tempCell = inCell(drawing.get(drawing.size()-1).x(),drawing.get(drawing.size()-1).y());
				if(tempCell !=null)
				{
					System.out.println("Ended inside a cell");
					//does it cross a port?
					tempPort = crossPort();
					if(tempPort!=null)
					{
						//DELETE PORT

						System.out.println("DELETE PORT!!!!!");
						tempCell.removePort(tempPort);
						tempCell.updatePortPositions();

					}

				}
				else
				{
					//does it cross a cell?
					tempCell = crossCell();
					if(tempCell!=null)
					{
						//DELETE CELL
						//if cell is an arrow node then delete all cells in it
						if(tempCell.isArrowNode())
						{
							for(int i = 0 ; i < tempCell.graph().cellSize() ; i++)
							{
								graph.removeCell(tempCell.graph().cell(i));
							}
						}
						graph.removeCell(tempCell);

					}

					//does it cross an edge?
					System.out.println("Entering crossEdges");
					LinkedList<Edge> temp = crossEdges();

					for(int i = 0 ; i< temp.size() ; i++)
					{
						Edge tempEdge = temp.get(i);
						temp.remove(i);
						graph.removeEdge(tempEdge);
						i--;
					}

				}
			}
		}
	}

	public void copyPortsFromType(Cell c)
	{
		Cell tempCell;

		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);

			if(tempCell !=c && tempCell.name().equals(c.name()))
			{
				for(int j =0 ; j < tempCell.portSize() ; j++)
				{
					c.addPort(tempCell.port(j));
				}

				break;
			}
		}
	}

	public void updateCellTypePortAdd(Cell c, Port p)
	{
		System.out.println("updateCellTypePortAdd entered");

		Cell tempCell;

		System.out.println("HELLO?");
		for(int i = 0 ; i < graph.cellSize() ; i++)
		{
			tempCell = graph.cell(i);
			//System.out.println("HELLO");
			//System.out.println("tempCell:"+tempCell.name() + "   c:"+c.name());

			if(tempCell !=c && tempCell.name().equals(c.name()))
			{
				System.out.println("Found a cell with the same name");
				tempCell.addPort(p);
			}
		}
	}

	public void updateCellTypePortName(Cell c, Port p)
	{
		Cell tempCell;

		for(int i = 0 ; i < selectedCells.size() ; i++)
		{
			tempCell = selectedCells.get(i);

			if(tempCell !=c && tempCell.name().equals(c.name()))
			{
				if(tempCell.port().contains(p))
				{

				}
			}
		}
	}

	public static boolean insidePolygon(LinkedList polygon,Pos p)
	{
		int i;
		float angle=0;
		Pos p1,p2;
		p1 = new Pos();
		p2= new Pos();

		Pos t1 = new Pos();
		Pos t2 = new Pos();

		int n = polygon.size()-1;

		for (i=0;i<n;i++) {

			t1 = (Pos)polygon.get(i);
			t2 = (Pos)polygon.get(i+1);

			p1.y(t1.y() - p.y());
			p1.x(t1.x() - p.x());
			p2.y(t2.y() - p.y());
			p2.x(t2.x() - p.x());
			angle += angle2D(p1.y(),p1.x(),p2.y(),p2.x());
		}

		if (Math.abs(angle) < Math.PI)
			return false;
		else
			return true;
	}

	public static float angle2D(float x1, float y1, float x2, float y2)
	{
		float dtheta,theta1,theta2;

		theta1 = (float)Math.atan2((double) y1,(double) x1);
		theta2 = (float)Math.atan2((double) y2,(double) x2);
		dtheta = theta2 - theta1;
		while (dtheta > Math.PI)
			dtheta -= Math.PI*2;
		while (dtheta < -Math.PI)
			dtheta += Math.PI*2;

		return dtheta;
	}


	public boolean lineInter(Pos v1, Pos v2, Pos v3, Pos v4)
	{
		float uat = (v4.x()-v3.x())*(v1.y()-v3.y()) - (v4.y()-v3.y())*(v1.x()-v3.x());

		float ubt = (v2.x()-v1.x())*(v1.y()-v3.y()) - (v2.y()-v1.y())*(v1.x()-v3.x());

		float bottom = (v4.y()-v3.y())*(v2.x()-v1.x()) - (v4.x()-v3.x())*(v2.y()-v1.y());

		if(bottom!=0)
		{
			float ua = uat/bottom;
			float ub = ubt/bottom;

			//System.out.println("ua= "+ua+"   ub= "+ub);

			if(((ua>=0)&&(ua<=1))&&((ub>=0)&&(ub<=1)))
				return true;
		}

		return false;
	}

	public float distance(float x1, float y1, float x2, float y2)
	{

		float x = x1 - x2;
		float y = y1 - y2;

		return (x*x) + (y*y);

	}

	public float curveValue( float newV, float oldV , float inc)
	{
		if(inc>1)
		{
			//if((oldV+360)-newV<newV-oldV)
			//	oldV=360+oldV;

			//if((newV+360)-oldV<oldV-newV)
			//	newV=360+newV;

			oldV=oldV-(oldV-newV)/inc;
		}
		if(inc<=1)
			return newV;

		return oldV;
	}

	public static void set2DMode(float x, float width, float y, float height) {
		glEnable(GL_TEXTURE_2D );
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glMatrixMode(GL11.GL_PROJECTION);                        // Select The Projection Matrix
		GL11.glPushMatrix();                                     // Store The Projection Matrix
		GL11.glLoadIdentity();                                   // Reset The Projection Matrix
		GL11.glOrtho(x, width, y, height, -1, 1);                          // Set Up An Ortho Screen
		GL11.glMatrixMode(GL11.GL_MODELVIEW);                         // Select The Modelview Matrix
		GL11.glPushMatrix();                                     // Store The Modelview Matrix
		GL11.glLoadIdentity();                                   // Reset The Modelview Matrix
	}
	public static void set3DMode() {
		glDisable(GL_TEXTURE_2D );
		GL11.glMatrixMode(GL11.GL_PROJECTION);                        // Select The Projection Matrix
		GL11.glPopMatrix();                                      // Restore The Old Projection Matrix
		GL11.glMatrixMode(GL11.GL_MODELVIEW);                         // Select The Modelview Matrix
		GL11.glPopMatrix();                                      // Restore The Old Projection Matrix
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}


}