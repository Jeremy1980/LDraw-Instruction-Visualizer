/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
	Copyright 2017 Jeremy Czajkowski <jeremy.cz@wp.pl>
	This file is part of JLDraw

	JLDraw is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	JLDraw is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with JLDraw.  If not, see <http://www.gnu.org/licenses/>.

*/


package jldraw;

import it.romabrick.matrix3d.Matrix3D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.common.nio.Buffers;

public class LDrawGLDisplay implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener {

	static { GLProfile.initSingleton(); }
		
	private GLCanvas canvas;
	private GL2 gl2;
	
	private boolean wireframe = true;
	private boolean polygon = true;
	private boolean lighting = true;
	private boolean perspective = false;
	private boolean bufferOk = false;
	private boolean selection = false; 
	private float zoomFactor = 1.0f;
	private float offsetx = 0f;
	private float offsety = 0f;
	private float offsetz = 0f;
	private Matrix3D viewMatrix = new Matrix3D();
	
	public static final int VERTEX = 0;
	public static final int VERTEX_COLOR = 1;
	public static final int LINE = 2;
	public static final int LINE_COLOR = 3;
	
	public static final int AXIS_X = 100;
	public static final int AXIS_Y = 200;
	public static final int AXIS_YX = 400;
	
	private LDRenderedModel model = null;
	private GLContext glcontext;
	private LinkedList<PartSelectionListener> selectionListeners = new LinkedList<PartSelectionListener>();
	private long drawingTime;
	private int centerx;
	private int centery;
	private int rotation;
	
	

	
	public LDrawGLDisplay() {
		GLProfile glp = GLProfile.getDefault();
		if (!glp.isGL2()) {
			throw new GLException("Your graphic card doesn't support requested OpenGL level.");
		}
		GLCapabilities caps = new GLCapabilities(glp);
		canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);
		canvas.setAutoSwapBufferMode(false);
		canvas.addMouseListener(this);
		canvas.addMouseWheelListener(this);
	}

	
	public GLCanvas getCanvas() {
		return canvas;
	}
	
	
	
	public void setWireframe(boolean wireframe) {
		this.wireframe = wireframe;
        canvas.repaint();
	}


	public void setPolygon(boolean polygon) {
		this.polygon = polygon;
        canvas.repaint();
	}


	public void setLighting(boolean lighting) {
		this.lighting = lighting;
        canvas.repaint();
	}


	public void setPerspective(boolean perspective) {
		this.perspective = perspective;
		canvas.repaint();
	}

	
	
	public void setZoomFactor(float zoom) {
		if (zoom <= 0.001) {
			throw new IllegalArgumentException("Zoom multiplier cannot be too low or negative");
		}
		zoomFactor *= zoom;
		canvas.repaint();
	}
	
	
	public void resetZoom() {
		
		zoomFactor = 1f;
		canvas.repaint();
	}



	public void setOffsetY(float offsety) {
		viewMatrix = viewMatrix.moveTo(0,-offsety, 0);
		canvas.repaint();
	}


	public void setOffsetX(float offsetx) {
		viewMatrix = viewMatrix.moveTo(-offsetx, 0, 0);
	}


	public void setOrigin(float x, float y, float z) {
		
		float f[] = viewMatrix.transformPoint(x, y, z);
		viewMatrix = viewMatrix.moveTo(-f[0], -f[1], -f[2]);
		canvas.repaint();
	}
	

	
	public void rotateX(float anglex) {
		viewMatrix = viewMatrix.rotateX((float)(anglex*Math.PI/180));
        canvas.repaint();
	}


	public void rotateY(float angley) {
		viewMatrix = viewMatrix.rotateY((float)(angley*Math.PI/180));
        canvas.repaint();
	}


	public void resetView() {
		
		viewMatrix = new Matrix3D();
		canvas.repaint();
	}
	
	

	public void placeModel(LDRenderedModel m) {
		
		if (model != null) {
		// there is a model in render area, free VA buffers
			clearVABuffers();
		}
		model = m;
        if (model != null) {
        	setVABuffers();
        	bufferOk = true;
        }
        canvas.repaint();
	}

	
	/**
	 * checks available OpenGL profile
	 */
	public static String checkGL() {
		
		GLProfile glp = GLProfile.getDefault();
		return glp.getName();
	}
	
	
	/*
	 * selection and selection listeners
	 */
	
	public boolean isSelectionEnabled() {
		return selection;
	}


	public void enableSelection(boolean selection) {
		this.selection = selection;
	}

	
	public void addPickListener(PartSelectionListener p) {
		
		if (p == null) 
			throw new NullPointerException("[LDrawGLDisplay] Cannot use a null SelectionListener");
		// avoid duplicate insert
		if (selectionListeners.contains(p))
				return;
		selectionListeners.add(p);
	}



	
	private void setVABuffers() {

		int[] vboArrayNames = new int[2];
		
		glcontext.makeCurrent();
		for (LDRenderedPart p : model.getParts()) {
			// gets and save array buffer names
			if (p.getTriangleVertexCount() > 0) {
		        gl2.glGenBuffers( 2, vboArrayNames, 0 );
		        p.setTriangleName(vboArrayNames[VERTEX]);
		        p.setTriangleColorName(vboArrayNames[VERTEX_COLOR]);
	        // store vertex coords
		        gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getTriangleName());
		        gl2.glBufferData( GL2.GL_ARRAY_BUFFER,
		                          p.getTrianglesVBO().length * Buffers.SIZEOF_FLOAT,
		                          null,
		                          GL2.GL_STATIC_DRAW );
		        ByteBuffer bytebuffer = gl2.glMapBuffer( GL2.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY );
		        FloatBuffer vertexbuffer = bytebuffer.order( ByteOrder.nativeOrder() ).asFloatBuffer();
		        vertexbuffer.put(p.getTrianglesVBO());
		        gl2.glUnmapBuffer( GL2.GL_ARRAY_BUFFER );
	        // store vertex colors
		        gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getTriangleColorName() );
		        gl2.glBufferData( GL2.GL_ARRAY_BUFFER,
		                          p.getTriangleColorVA().length * Buffers.SIZEOF_BYTE,
		                          null,
		                          GL2.GL_STATIC_DRAW );
		        bytebuffer = gl2.glMapBuffer( GL2.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY );
		        ByteBuffer vertexColorBuffer = bytebuffer.order( ByteOrder.nativeOrder() );
		        vertexColorBuffer.put(p.getTriangleColorVA());
		        gl2.glUnmapBuffer( GL2.GL_ARRAY_BUFFER );
			}
	        // store line coords
	        if (p.getLineVertexCount() > 0) {
		        gl2.glGenBuffers( 2, vboArrayNames, 0 );
		        p.setLineName(vboArrayNames[VERTEX]);
		        p.setLineColorName(vboArrayNames[VERTEX_COLOR]);
		        gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getLineName());
		        gl2.glBufferData( GL2.GL_ARRAY_BUFFER,
		                          p.getWireFrameVBO().length * Buffers.SIZEOF_FLOAT,
		                          null,
		                          GL2.GL_STATIC_DRAW );
		        ByteBuffer bytebuffer = gl2.glMapBuffer( GL2.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY );
		        FloatBuffer vertexbuffer = bytebuffer.order( ByteOrder.nativeOrder() ).asFloatBuffer();
		        vertexbuffer.put(p.getWireFrameVBO());
		        gl2.glUnmapBuffer( GL2.GL_ARRAY_BUFFER );
	        // store line colors
		        gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getLineColorName());
		        gl2.glBufferData( GL2.GL_ARRAY_BUFFER,
		                          p.getWireColorVa().length * Buffers.SIZEOF_BYTE,
		                          null,
		                          GL2.GL_STATIC_DRAW );
		        bytebuffer = gl2.glMapBuffer( GL2.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY );
		        ByteBuffer lineColorBuffer = bytebuffer.order( ByteOrder.nativeOrder() );
		        lineColorBuffer.put(p.getWireColorVa());
		        gl2.glUnmapBuffer( GL2.GL_ARRAY_BUFFER );
	        }
	        if (p.getAuxLineVertexCount() > 0) {
		        gl2.glGenBuffers( 2, vboArrayNames, 0 );
		        p.setAuxLineName(vboArrayNames[VERTEX]);
		        p.setAuxLineColorName(vboArrayNames[VERTEX_COLOR]);
		        gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getAuxLineName());
		        gl2.glBufferData( GL2.GL_ARRAY_BUFFER,
		                          p.getAuxWireFrameVBO().length * Buffers.SIZEOF_FLOAT,
		                          null,
		                          GL2.GL_STATIC_DRAW );
		        ByteBuffer bytebuffer = gl2.glMapBuffer( GL2.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY );
		        FloatBuffer vertexbuffer = bytebuffer.order( ByteOrder.nativeOrder() ).asFloatBuffer();
		        vertexbuffer.put(p.getAuxWireFrameVBO());
		        gl2.glUnmapBuffer( GL2.GL_ARRAY_BUFFER );
	        // store line colors
		        gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getAuxLineColorName());
		        gl2.glBufferData( GL2.GL_ARRAY_BUFFER,
		                          p.getAuxWireColorVa().length * Buffers.SIZEOF_BYTE,
		                          null,
		                          GL2.GL_STATIC_DRAW );
		        bytebuffer = gl2.glMapBuffer( GL2.GL_ARRAY_BUFFER, GL2.GL_WRITE_ONLY );
		        ByteBuffer lineColorBuffer = bytebuffer.order( ByteOrder.nativeOrder() );
		        lineColorBuffer.put(p.getAuxWireColorVa());
		        gl2.glUnmapBuffer( GL2.GL_ARRAY_BUFFER );
	        }
		}
		glcontext.release();
	}
	
	
	
	/* 
	 * releases vertex and object buffers
	 */
	void clearVABuffers() {
		
		bufferOk = false;
		if (model == null)
			return;
		glcontext.makeCurrent();
		for (LDRenderedPart p : model.getParts()) {
			if (p.getLineVertexCount() > 0) {
				gl2.glDeleteBuffers(2, new int[] {p.getLineName(),p.getLineColorName()},0);
			}
			if (p.getAuxLineVertexCount() > 0) {
				gl2.glDeleteBuffers(2, new int[] {p.getAuxLineName(),p.getAuxLineColorName()},0);
			}
			if (p.getTriangleVertexCount() > 0) {
				gl2.glDeleteBuffers(2, new int[] {p.getTriangleName(),p.getTriangleColorName()},0);
			}			
		}
		glcontext.release();
	}
	
	
	
	
	public long getDrawTimeMs() {
		return drawingTime;
	}
	
	
	
	
	private void render(GLDrawable drawable) {

		gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();  // Reset The Projection Matrix
        //
        GLU glu = new GLU();
        int width = drawable.getSurfaceWidth();
        int height = drawable.getSurfaceHeight() == 0 ? 1 : drawable.getSurfaceHeight();
        if (perspective) {
        	glu.gluPerspective(40f, (float)width/height, 1f, 10000f);
        	glu.gluLookAt(0, 0, -800*zoomFactor, 0, 0, 0, 0, -1, 0);
        }
        else {
        	gl2.glOrthof(-width*zoomFactor/2,width*zoomFactor/2,-height*zoomFactor/2,height*zoomFactor/2, -2000, 2000);
        	glu.gluLookAt(0, 0, -500, 0, 0, 0, 0, -1, 0);
        }
        gl2.glViewport( 0, 0, width, height );
        
        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();
        gl2.glMultMatrixf(viewMatrix.getAsOpenGLMatrix(), 0);
        gl2.glTranslatef(-offsetx, -offsety, -offsetz);
        
        gl2.glClearColor(0.90f, 0.90f, 0.90f, 0f);    // This Will Clear The Background Color
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);    // Clear The Screen And The Depth Buffer
        gl2.glColorMaterial( GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE );
        
        if (bufferOk) {
            gl2.glEnableClientState( GL2.GL_VERTEX_ARRAY );
	        for (LDRenderedPart p : model.getParts()) {
            // draw triangles and lines
	            gl2.glEnableClientState( GL2.GL_COLOR_ARRAY );
	        	if (p.getTriangleVertexCount() > 0 && polygon && !p.isHidden()) {
	                if (lighting)
	                	gl2.glEnable(GL2.GL_LIGHTING);
		            gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		            gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getTriangleName() );
		            gl2.glVertexPointer( 3, GL2.GL_FLOAT, 6 * Buffers.SIZEOF_FLOAT, 0 );
		            gl2.glNormalPointer(GL2.GL_FLOAT,6 * Buffers.SIZEOF_FLOAT ,3 * Buffers.SIZEOF_FLOAT);
		            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, p.getTriangleColorName());
		            gl2.glColorPointer( 4, GL2.GL_UNSIGNED_BYTE, 4 * Buffers.SIZEOF_BYTE, 0 );
		            gl2.glDrawArrays( GL2.GL_TRIANGLES, 0, p.getTriangleVertexCount() );
		            gl2.glDisableClientState( GL2.GL_NORMAL_ARRAY );	
		            gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, 0 );
		            gl2.glDisable(GL2.GL_LIGHTING);
	        	}
	        	if (p.isSelected() && !p.isHidden()) {
		            gl2.glDisableClientState( GL2.GL_COLOR_ARRAY );
		            gl2.glLineWidth(3f);
		            gl2.glColor4f(0.6f, 1f, 0.5f,1f);
		            if (p.getLineVertexCount() > 0) {
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, p.getLineName());
			            gl2.glVertexPointer( 3, GL2.GL_FLOAT, 3 * Buffers.SIZEOF_FLOAT, 0 );
			            gl2.glDrawArrays( GL2.GL_LINES, 0, p.getLineVertexCount() );
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		            }
		            if (p.getAuxLineVertexCount() > 0) {
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, p.getAuxLineName());
			            gl2.glVertexPointer( 3, GL2.GL_FLOAT, 3 * Buffers.SIZEOF_FLOAT, 0 );
			            gl2.glDrawArrays( GL2.GL_LINES, 0, p.getAuxLineVertexCount() );
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		            }
		            gl2.glEnableClientState( GL2.GL_COLOR_ARRAY );
		            gl2.glLineWidth(1f);
	        	}
	        	else if (wireframe && !p.isHidden()) {
	        		if (p.getLineVertexCount() > 0) {
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, p.getLineName());
			            gl2.glVertexPointer( 3, GL2.GL_FLOAT, 3 * Buffers.SIZEOF_FLOAT, 0 );
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, p.getLineColorName());
			            gl2.glColorPointer( 4, GL2.GL_UNSIGNED_BYTE, 4 * Buffers.SIZEOF_BYTE, 0 );
			            gl2.glDrawArrays( GL2.GL_LINES, 0, p.getLineVertexCount() );
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	        		}
		            if (!polygon && p.getAuxLineVertexCount() > 0) {
        	// display aux lines only if polygons are hidden
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, p.getAuxLineName());
			            gl2.glVertexPointer( 3, GL2.GL_FLOAT, 3 * Buffers.SIZEOF_FLOAT, 0 );
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, p.getAuxLineColorName());
			            gl2.glColorPointer( 4, GL2.GL_UNSIGNED_BYTE, 4 * Buffers.SIZEOF_BYTE, 0 );
			            gl2.glDrawArrays( GL2.GL_LINES, 0, p.getAuxLineVertexCount() );
			            gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		            }
	            }
	            gl2.glDisableClientState( GL2.GL_COLOR_ARRAY );
	        }
            gl2.glDisableClientState( GL2.GL_VERTEX_ARRAY );
        }

	}
	
	
	
	
	@Override
	public void display(GLAutoDrawable drawable) {
		
		long t0 = System.nanoTime();
		glcontext.makeCurrent();
		
		render(drawable);
        
        canvas.swapBuffers();
        if (selection) {
        	// draw in back-buffer same model with color-mode selection
            if (bufferOk) {
                gl2.glClearColor(0, 0, 0, 255);    // background is solid black
            	
            	gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	            gl2.glEnableClientState( GL2.GL_VERTEX_ARRAY );
    	        for (LDRenderedPart p : model.getParts()) {
    	        	gl2.glColor3ub(
    	        			(byte)((p.getId()&0xff0000)>>16), 
    	        			(byte) ((p.getId()&0xff00)>>8), 
    	        			(byte)(p.getId()&0xff));
    	        	if (p.getTriangleVertexCount() > 0 && !p.isHidden()) {
    		            gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, p.getTriangleName() );
    		            gl2.glVertexPointer( 3, GL2.GL_FLOAT, 6 * Buffers.SIZEOF_FLOAT, 0 );
    		            gl2.glDrawArrays( GL2.GL_TRIANGLES, 0, p.getTriangleVertexCount() );
    		            gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, 0 );
    	        	}
    	        }
	            gl2.glDisableClientState( GL2.GL_VERTEX_ARRAY );
            }
        }

        glcontext.release();
        drawingTime = System.nanoTime()-t0;
        if (gl2.glGetError() != 0)
        	System.out.println("[LDrawGLDisplay] " +gl2.glGetError());
	}

	@Override
	public void dispose(GLAutoDrawable drawable) { }

	@Override
	public void init(GLAutoDrawable drawable) {
		
		glcontext = drawable.getContext();
		glcontext.makeCurrent();
		
		gl2 = drawable.getGL().getGL2();
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, new float[] {5f,-9f,-10f,0f}, 0);
        gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT,new float[] { 0.2f, 0.2f, 0.2f, 1f },0);
        gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, new float[] {0.9f,0.9f,0.9f,1f}, 0);
        gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE,new float[] { 0.8f, 0.8f, 0.8f, 1f },0);        

        gl2.glEnable(GL2.GL_COLOR_MATERIAL);
        gl2.glColorMaterial( GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE );
        gl2.glDisable(GL2.GL_LIGHT0);
        gl2.glEnable(GL2.GL_LIGHT1);
        gl2.glEnable(GL2.GL_LIGHTING);
        
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl2.glEnable(GL2.GL_BLEND); 
        gl2.glLineWidth(1f);
        gl2.glClearDepth(1.0);            // Enables Clearing Of The Depth Buffer
        gl2.glDepthFunc(GL2.GL_LESS);     // The Type Of Depth Test To Do
        gl2.glEnable(GL2.GL_DEPTH_TEST);  // Enables Depth Testing

        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();  			  // Reset The Projection Matrix
        int glerror = gl2.glGetError(); 
        if (glerror != 0)
        	System.out.println("[LDrawGLDisplay] " + Integer.toHexString(glerror));
        glcontext.release();

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) { canvas.repaint(); }

	
	
	public void update() {
		
		canvas.repaint();
	}
	
	
	
	/*
	 * version without AWTGLReadBufferUtil that works on OSX Mavericks
	 */
	public BufferedImage getScreenShot() {
		
		glcontext.makeCurrent();
		render(glcontext.getGLDrawable());
		int w = glcontext.getGLDrawable().getSurfaceWidth();
		int h = glcontext.getGLDrawable().getSurfaceHeight();
		
		// with this setting on OSX Mavericks images are broken: use no alpha channel
		BufferedImage image = new BufferedImage(w,h, BufferedImage.TYPE_3BYTE_BGR);
		DataBufferByte awfulBufferBack = (DataBufferByte) image.getRaster().getDataBuffer();
		Buffer b = ByteBuffer.wrap(awfulBufferBack.getData());
		glcontext.getGL().getGL2().glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
		
		// broken images on OSX Mavericks if alpha channel is included
		glcontext.getGL().getGL2().glReadPixels(0, 0, w, h, GL2.GL_BGR, GL2.GL_UNSIGNED_BYTE, b);
		glcontext.release();
		canvas.repaint();
		
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
	    tx.translate(0,-image.getHeight());
	    AffineTransformOp op = new AffineTransformOp(tx,
	        AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	    image = op.filter(image, null);
		return image;
	}
	
	
	
	/* 
	 * mouse listeners for part picking (non Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */

	/**
	 * callback PartSelectionListeners to notify of part selection
	 * 
	 * listeners must implement PartSelectionListener interface
	 * Listener called with:
	 * partId - global part id (0 if no part selected)
	 * PickMode - mode of picking (NONE, ADD, TOGGLE) 
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		
		// only button1 or button3 (left or right)
		int button = e.getButton();
		if ((button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) && 
				bufferOk) {
			glcontext.makeCurrent();
			int clickedX = e.getX();
			int clickedY = canvas.getHeight()-e.getY();
			ByteBuffer b = ByteBuffer.allocateDirect(4);
			b.order(ByteOrder.nativeOrder());
			gl2.glReadPixels(clickedX, clickedY, 1, 1, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, b);
			int selectedId = (int) (b.get(0)&0xff)*65536+(int)(b.get(1)&0xff)*256+(int)(b.get(2)&0xff);
			glcontext.release();
		// select pick mode
			PickMode pm = PickMode.NONE;
			if (e.isControlDown()) {
				pm = PickMode.TOGGLE;
			}
			else if (e.isShiftDown()) {
				pm = PickMode.ADD;
			}
			if (button == MouseEvent.BUTTON3) {
		// special case: context menu for selected part
				pm = PickMode.POPUP_MENU;
			}
		// calls listeners
			for (PartSelectionListener p:selectionListeners) {
				p.partPicked(selectedId, pm, e);
			}
		}
	}

	
	@Override
	public void mousePressed(MouseEvent e) { 
		
		// for mouse rotation only with ALT (key)
		if (!e.isAltDown())
			return;
		canvas.addMouseMotionListener(this);
		centerx = e.getX();
		centery = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) { 
		
		if (!e.isAltDown())
			return;
		canvas.removeMouseMotionListener(this);
	}

	@Override
	public void mouseEntered(MouseEvent e) { 
		
	}

	@Override
	public void mouseExited(MouseEvent e) { 
		
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		
		// for mouse rotation only with ALT (key)
		if (!e.isAltDown())
			return;
		float factor = (float)Math.PI/(canvas.getWidth()>360?canvas.getWidth():360) ;
		int deltax = centerx - e.getX();
		int deltay = centery - e.getY();
		centerx = e.getX();
		centery = e.getY();
		// it is a pan
		//	viewMatrix = viewMatrix.moveTo(-deltax*zoomFactor, -deltay*zoomFactor, 0);
		//it is a rotation
			Matrix3D matrix = viewMatrix.getCopy();
			switch(rotation) {
			case LDrawGLDisplay.AXIS_Y:
				viewMatrix = matrix.rotateX(deltay*factor);
				break;
			case LDrawGLDisplay.AXIS_X:
				viewMatrix = matrix.rotateY(deltax*factor);
				break;
			default:
				viewMatrix = matrix.rotateY(-deltax*factor) .rotateX(deltay*factor);
			}
			canvas.repaint();
 	}


	@Override
	public void mouseMoved(MouseEvent e) { }


	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		int r = e.getWheelRotation();
		if (r < 0) {
			zoomFactor *= 0.9f;
		}
		else {
			zoomFactor *= 1.1f;
		}
		canvas.repaint();
	}


	public Matrix3D getMatrix() {
		return viewMatrix.getCopy();
	}


	public void applyMatrix(Matrix3D matrix) {
		if (matrix != null) {
			this.viewMatrix = matrix;
			canvas.repaint();
		}
	}


	public void setAxisRotation(int axis) {
		this.rotation = axis;
	}



}
