package com.anz.rtl.transactions.response;

import io.swagger.annotations.ApiModelProperty;

public class MerchantLogo {

	@ApiModelProperty(value = "Represents the url of the logo.", example = "https://images.lookwhoscharging.com/1001141992/ANZ-ATM--Bourke-St-cirl-image.png", position = 52)
	private String url;
	@ApiModelProperty(value = "Represents the height of the logo.", example = "128", position = 53)
	private Integer height;
	@ApiModelProperty(value = "Represents the width of the logo.", example = "128", position = 54)
	private Integer width;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}
}
