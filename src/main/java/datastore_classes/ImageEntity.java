package datastore_classes;

import java.awt.Color;

import com.googlecode.objectify.annotation.*;

@Entity
public class ImageEntity {

	@Id
	long id;
	
	private String base64Code;
	
	@Index
	private String type;
	
	@Index
	private int color;
	
	public ImageEntity() {
		base64Code = "";
		type = "";
		color = 0;
	}
	
	public ImageEntity(long id, String base64Code, String type, Color color) {
		this.id = id;
		this.base64Code = base64Code;
		this.type = type;
		this.color = color.getRGB();
	}

	public String getBase64Code() {
		return base64Code;
	}

	public String getType() {
		return type;
	}

	public int getColor() {
		return color;
	}
	
}

