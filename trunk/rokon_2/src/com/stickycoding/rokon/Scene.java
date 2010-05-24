package com.stickycoding.rokon;

import javax.microedition.khronos.opengles.GL10;

import android.view.MotionEvent;

/**
 * Scene.java
 * A Scene holds and prepares drawable objects or object groups
 * 
 * @author Richard
 */
public class Scene {
	
	public static final int SCENE_TEXTURE_COUNT = 32;
	public static final int DEFAULT_LAYER_COUNT = 1;
	public static final int DEFAULT_LAYER_OBJECT_COUNT = 32;

	protected Layer[] layer;
	protected boolean loadedTextures;
	protected int layerCount;
	protected Window window = null;
	protected Texture[] textures;

	public void onTouchDown(DrawableObject object, float x, float y, MotionEvent event) { }
	public void onTouchUp(DrawableObject object, float x, float y, MotionEvent event) { }
	public void onTouchMove(DrawableObject object, float x, float y, MotionEvent event) { }
	public void onTouch(DrawableObject object, float x, float y, MotionEvent event) { }
	public void onTouchDown(float x, float y, MotionEvent event) { }
	public void onTouchMove(float x, float y, MotionEvent event) { }
	public void onTouch(float x, float y, MotionEvent event) { }
	public void onTouchUp(float x, float y, MotionEvent event) { }
	
	protected void handleTouch(MotionEvent event) {
		event.setLocation(event.getX() * (Device.widthPixels / RokonActivity.gameWidth), event.getY() * (Device.heightPixels  / RokonActivity.gameHeight));
		onTouch(event.getX(), event.getY(), event);
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onTouchDown(event.getX(), event.getY(), event);
				break;
			case MotionEvent.ACTION_UP:
				onTouchUp(event.getX(), event.getY(), event);
				break;
			case MotionEvent.ACTION_MOVE:
				onTouch(event.getX(), event.getY(), event);
				break;
		}
		for(int i = 0; i < layerCount; i++) {
			for(int j = 0; j < layer[i].maximumDrawableObjects; j++) {
				DrawableObject object = layer[i].drawableObjects.get(j);
				if(object != null && object.isTouchable) {
					if(MathHelper.pointInRect(event.getX(), event.getY(), object.x, object.y, object.width, object.height)) {
						onTouch(object, event.getX(), event.getY(), event);
						switch(event.getAction()) {
							case MotionEvent.ACTION_DOWN:
								onTouchDown(object, event.getX(), event.getY(), event);
								break;
							case MotionEvent.ACTION_UP:
								onTouchUp(object, event.getX(), event.getY(), event);
								break;
							case MotionEvent.ACTION_MOVE:
								onTouch(object, event.getX(), event.getY(), event);
								break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new Scene with given layer count, and a corresponding maximum DrawableObject count 
	 * 
	 * @param layerCount maximum number of layers
	 * @param layerObjectCount maximum number of DrawableObjects per layer, the array length must match layerCount
	 */
	public Scene(int layerCount, int[] layerObjectCount) {
		this.layerCount = layerCount;
		layer = new Layer[layerCount];
		for(int i = 0; i < layerCount; i++) {
			layer[i] = new Layer(this, layerObjectCount[i]);
		}
		prepareNewScene();
	}
	
	/**
	 * Creates a new Scene with given layer count, all layers will have the same maximum number of DrawableObjects
	 * 
	 * @param layerCount maximum number of layers
	 * @param layerObjectCount maximum number of DrawableObjects per layer
	 */
	public Scene(int layerCount, int layerObjectCount) {
		this.layerCount = layerCount;
		layer = new Layer[layerCount];
		for(int i = 0; i < layerCount; i++) {
			layer[i] = new Layer(this, layerObjectCount);
		}
		prepareNewScene();
	}
	
	/**
	 * Creates a new Scene with given layer count, and a default maximum DrawableObject count of DEFAULT_LAYER_OBJECT_COUNT
	 * 
	 * @param layerCount maximum number of layers
	 */
	public Scene(int layerCount) {
		this(layerCount, DEFAULT_LAYER_OBJECT_COUNT);
	}
	
	/**
	 * Creates a new Scene with defaults, DEFAULT_LAYER_COUNT and DEFAULT_LAYER_OBJECT_COUNT
	 */
	public Scene() {
		this(DEFAULT_LAYER_COUNT, DEFAULT_LAYER_OBJECT_COUNT);
		
	}
	
	private void prepareNewScene() {
		textures = new Texture[SCENE_TEXTURE_COUNT];
	}
	
	/**
	 * Flags a Texture to be loaded into this Scene
	 * This must be called before RokonActivity.setScene
	 * 
	 * @param texture valid Texture object
	 */
	public void useTexture(Texture texture) {
		for(int i = 0; i < textures.length; i++) {
			if(textures[i] == null) {
				textures[i] = texture;
				return;
			}
		}
		Debug.warning("Scene.useTexture", "Tried loading too many Textures onto the Scene, max is " + textures.length);
	}
	
	/**
	 * Defines the active Window for this Scene
	 * If no Window is given, a default static view will be rendered 
	 * 
	 * @param window
	 */
	public void setWindow(Window window) {
		if(window == null) {
			Debug.warning("Scene.setWindow", "Tried setting a NULL Window");
			return;
		}
		this.window = window;
	}
	
	/**
	 * Removes the current active Window, returning it to NULL
	 */
	public void removeWindow() {
		window = null;
	}
	
	/**
	 * @return NULL if there is no Window associated with this Scene
	 */
	public Window getWindow() {
		if(window == null)
			return null;
		return window;
	}
	
	/**
	 * Fetches the Layer object associated with the given index
	 * 
	 * @param index the index of the Layer
	 * @return NULL if invalid index is given
	 */
	public Layer getLayer(int index) {
		if(index < 0 || index > layerCount) {
			Debug.warning("Scene.getLayer", "Tried fetching invalid layer (" + index + "), maximum is " + layerCount);
			return null;
		}
		return layer[index];
	}
	
	/**
	 * Clears the DrawableObjects from all Layers
	 */
	public void clear() {
		for(int i = 0; i < layerCount; i++) {
			layer[i].clear();
		}
	}
	
	/**
	 * Clears all the DrawableObjects from a specified Layer
	 * 
	 * @param index the index of the Layer
	 */
	public void clearLayer(int index) {
		if(index <= 0 || index > layerCount) {
			Debug.warning("Scene.clearLayer", "Tried clearing invalid layer (" + index + "), maximum is " + layerCount);
			return;
		}
		layer[index].clear();
	}
	
	/**
	 * Moves a Layer from one index to another, and shuffles the others up or down to accomodate
	 * 
	 * @param startIndex the current index of the Layer
	 * @param endIndex the desired final index of the Layer
	 */
	public void moveLayer(int startIndex, int endIndex) {
		if(startIndex == endIndex) {
			Debug.warning("Scene.moveLayer", "Tried moving a Layer to its own position, stupid");
			return;
		}
		if(startIndex <= 0 || startIndex > layerCount) {
			Debug.warning("Scene.moveLayer", "Tried moving an invalid Layer, startIndex=" + startIndex + ", maximum is " + layerCount);
			return;
		}
		if(endIndex <= 0 || endIndex > layerCount) {
			Debug.warning("Scene.moveLayer", "Tried moving an invalid Layer, endIndex=" + endIndex + ", maximum is " + layerCount);
			return;
		}
		Layer temporaryLayer = layer[startIndex];
		if(endIndex < startIndex) {
			for(int i = endIndex; i < startIndex; i++) {
				layer[i + 1] = layer[i];
			}
			layer[endIndex] = temporaryLayer;
		}
		if(endIndex > startIndex) { 
			for(int i = startIndex; i < endIndex; i++) {
				layer[i] = layer[i + 1];
			}
			layer[endIndex] = temporaryLayer;
		}
	}
	
	/**
	 * Switches the position of one Layer with another
	 * 
	 * @param layer1 the index of the first Layer
	 * @param layer2 the index of the second Layer
	 */
	public void switchLayers(int layer1, int layer2) {
		if(layer1 == layer2) {
			Debug.warning("Scene.switchLayers", "Tried switching the same Layer");
			return;
		}
		if(layer1 < 0 || layer1 > layerCount) {
			Debug.warning("Scene.switchLayers", "Tried switch an invalid Layer, layer1=" + layer1 + ", maximum is " + layerCount);
			return;
		}
		if(layer2 < 0 || layer2 > layerCount) {
			Debug.warning("Scene.switchLayers", "Tried switch an invalid Layer, layer2=" + layer2 + ", maximum is " + layerCount);
			return;
		}
		Layer temporaryLayer = layer[layer1];
		layer[layer1] = layer[layer2];
		layer[layer2] = temporaryLayer;
	}
	
	/**
	 * Replaces a Layer object in this Scene
	 * 
	 * @param index a valid index for a Layer, less than getLayerCount
	 * @param layer a valid Layer object to replace the existing Layer
	 */
	public void setLayer(int index, Layer layer) {
		if(layer == null) {
			Debug.warning("Scene.setLayer", "Tried setting to a null Layer");
			return;
		}
		if(index < 0 || index > layerCount) {
			Debug.warning("Scene.setLayer", "Tried setting an invalid Layer, index=" + index + ", maximum is " + layerCount);
			return;
		}
		this.layer[index] = layer;
	}
	
	/**
	 * Adds a DrawableObject to the first (0th) Layer
	 * 
	 * @param drawableObject a valid DrawableObject
	 */
	public void add(DrawableObject drawableObject) {
		layer[0].add(drawableObject);
	}
	
	/**
	 * Adds a DrawableObject to a given Layer
	 * 
	 * @param layerIndex a valid index of a Layer
	 * @param drawableObject a valid DrawableObject
	 */
	public void add(int layerIndex, DrawableObject drawableObject) {
		if(layerIndex < 0 || layerIndex > layerCount) {
			Debug.warning("Scene.add", "Tried adding to an invalid Layer, layerIndex=" + layerIndex + ", maximum is " + layerCount);
			return;
		}
		if(drawableObject == null) {
			Debug.warning("Scene.add", "Tried adding a NULL DrawableObject");
			return;
		}
		layer[layerIndex].add(drawableObject);
	}
	
	/**
	 * Removes a DrawableObject from the Scene
	 * 
	 * @param drawableObject a valid DrawableObject
	 */
	public void remove(DrawableObject drawableObject) {
		drawableObject.remove();
	}
	
	protected void onUpdate() {
		
	}
	
	protected void onGameLoop() {
		
	}
	
	protected void onSetScene() {
		loadedTextures = false;
	}
	
	protected void onEndScene() {
		
	}
	
	protected void onLoadTextures(GL10 gl) {
		Debug.print("Loading textures onto the Scene");
		for(int i = 0; i < textures.length; i++) {
			if(textures[i] != null) {
				textures[i].onLoadTexture(gl);
				textures[i] = null;
			}
		}
		loadedTextures = true;
	}
	
	protected void onDraw(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
		for(int i = 0; i < layerCount; i++) {
			layer[i].onDraw(gl);
		}
	}
	
}