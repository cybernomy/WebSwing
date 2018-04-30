package org.webswing.model.s2c;

import java.util.List;

import org.webswing.model.Msg;

public class WindowMsg implements Msg {

	private static final long serialVersionUID = -523823816533325842L;
	private String id;
	private List<WindowPartialContentMsg> content;
	private byte[] directDraw;
	private String title;
	private Integer posX;
	private Integer posY;
	private Integer width;
	private Integer height;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<WindowPartialContentMsg> getContent() {
		return content;
	}

	public void setContent(List<WindowPartialContentMsg> content) {
		this.content = content;
	}

	public byte[] getDirectDraw() {
		return directDraw;
	}

	public void setDirectDraw(byte[] directDraw) {
		this.directDraw = directDraw;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getPosX() {
		return posX;
	}

	public void setPosX(Integer posX) {
		this.posX = posX;
	}

	public Integer getPosY() {
		return posY;
	}

	public void setPosY(Integer posY) {
		this.posY = posY;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

}
