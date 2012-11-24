/**
 * Copyright 2010-2012 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ralscha.extdirectspring.generator;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Represents one field in a {@link ModelBean}
 * 
 * @author Ralph Schaer
 */
@JsonInclude(Include.NON_NULL)
public class ModelFieldBean {
	private String name;

	private ModelType type;

	private Object defaultValue;

	private String dateFormat;

	private Boolean useNull;
	
	private String mapping;
	
	//only a false value will be generated
	private Boolean persist = null;

	@JsonRawValue
	private String convert;

	/**
	 * Creates a new ModelFieldBean with name and type
	 * 
	 * @param name name of the field
	 * @param type type of the field
	 */
	public ModelFieldBean(String name, ModelType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	/**
	 * Name of the field. Property '<a
	 * href="http://docs.sencha.com/ext-js/4-1/#!/api/Ext.data.Field-cfg-name"
	 * >name</a>' in JS.
	 * 
	 * @param name new name for the field
	 */
	public void setName(String name) {
		this.name = name;
	}

	@JsonSerialize(using = ModelTypeSerializer.class)
	public ModelType getType() {
		return type;
	}

	/**
	 * Type of the field. Property '<a
	 * href="http://docs.sencha.com/ext-js/4-1/#!/api/Ext.data.Field-cfg-type"
	 * >type</a>' in JS.
	 * 
	 * @param type new type for the field
	 */
	public void setType(ModelType type) {
		this.type = type;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * The default value. Property '<a href=
	 * "http://docs.sencha.com/ext-js/4-1/#!/api/Ext.data.Field-cfg-defaultValue"
	 * >defaultValue</a>' in JS.
	 * 
	 * @param defaultValue new defaultValue
	 */
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * Specifies format of date. Property '<a href=
	 * "http://docs.sencha.com/ext-js/4-1/#!/api/Ext.data.Field-cfg-dateFormat"
	 * >dateFormat</a>' in JS.<br>
	 * For a list of all supported formats see Sencha Doc: <a
	 * href="http://docs.sencha.com/ext-js/4-1/#!/api/Ext.Date">Ext.Date</a>
	 * <p>
	 * Will be ignored if the field is not a {@link ModelType#DATE} field.
	 * 
	 * @param dateFormat new dateFormat String
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Boolean getUseNull() {
		return useNull;
	}

	/**
	 * If true null value is used if value cannot be parsed. If false default
	 * values are used (0 for integer and float, "" for string and false for
	 * boolean).<br>
	 * Property '<a href=
	 * "http://docs.sencha.com/ext-js/4-1/#!/api/Ext.data.Field-cfg-useNull"
	 * >useNull</a>' in JS.<br>
	 * <p>
	 * Only used if type of field is {@link ModelType#INTEGER},
	 * {@link ModelType#FLOAT}, {@link ModelType#STRING} or
	 * {@link ModelType#BOOLEAN}.
	 * 
	 * @param useNull new value for useNull
	 */
	public void setUseNull(Boolean useNull) {
		this.useNull = useNull;
	}
	
	public String getMapping() {
		return mapping;
	}

	/**
	 * TODO doc setMapping
	 * @param mapping
	 */
	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	public Boolean getPersist() {
		return persist;
	}

	/**
	 * TODO doc setPersist
	 * @param persist
	 */
	public void setPersist(Boolean persist) {
		this.persist = persist;
	}

	public String getConvert() {
		return convert;
	}

	/**
	 * TODO doc setConvert
	 * @param convert
	 */
	public void setConvert(String convert) {
		this.convert = convert;
	}

	private final static class ModelTypeSerializer extends JsonSerializer<ModelType> {
		@Override
		public void serialize(ModelType value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonProcessingException {
			jgen.writeString(value.getJsName());

		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((convert == null) ? 0 : convert.hashCode());
		result = prime * result + ((dateFormat == null) ? 0 : dateFormat.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((persist == null) ? 0 : persist.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((useNull == null) ? 0 : useNull.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelFieldBean other = (ModelFieldBean) obj;
		if (convert == null) {
			if (other.convert != null)
				return false;
		} else if (!convert.equals(other.convert))
			return false;
		if (dateFormat == null) {
			if (other.dateFormat != null)
				return false;
		} else if (!dateFormat.equals(other.dateFormat))
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (mapping == null) {
			if (other.mapping != null)
				return false;
		} else if (!mapping.equals(other.mapping))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (persist == null) {
			if (other.persist != null)
				return false;
		} else if (!persist.equals(other.persist))
			return false;
		if (type != other.type)
			return false;
		if (useNull == null) {
			if (other.useNull != null)
				return false;
		} else if (!useNull.equals(other.useNull))
			return false;
		return true;
	}
}
