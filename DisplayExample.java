import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

public class DisplayExample {

	public void start() {
		try {
			Display.setDisplayMode(new DisplayMode(800,600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// init OpenGL here

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800, 600, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		while (!Display.isCloseRequested()) {

			// render OpenGL here
			
			// Clear the screen and depth buffer
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	
					
			// set the color of the quad (R,G,B,A)
			glColor3f(0.5f,0.5f,1.0f);
				
			// draw quad
			glBegin(GL_QUADS);
			    glVertex2f(100,100);
			    glVertex2f(100+200,100+40);
			    glVertex2f(100+200,100+200);
			    glVertex2f(100,100+200);
			    glEnd();

			Display.update();
		}

		Display.destroy();
	}

	public static void main(String[] argv) {
		DisplayExample displayExample = new DisplayExample();
		displayExample.start();
	}
}
