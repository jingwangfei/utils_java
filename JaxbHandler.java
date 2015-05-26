package com.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/*
 * java 与  pojo 进行互转的工具类
 */
public class JaxbHandler {

	private static final Logger logger = Logger.getLogger(JaxbHandler.class);
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	public JaxbHandler(String contextPath) {
		this(null, contextPath);
	}

	public JaxbHandler(String xsdPath, String contextPath) {
		try {
			this.jaxbContext = JAXBContext.newInstance(contextPath);
			this.marshaller = this.jaxbContext.createMarshaller();
			this.unmarshaller = this.jaxbContext.createUnmarshaller();

			if (null != xsdPath) {
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				URL source = this.getClass().getClassLoader().getResource(xsdPath);
				Schema schema = schemaFactory.newSchema(source);

				ValidationEventCollector vec = new ValidationEventCollector();

				this.marshaller.setSchema(schema); // 设置schema校验
				this.marshaller.setEventHandler(vec);
				this.marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); // 设置编码

				this.unmarshaller.setSchema(schema);
				this.unmarshaller.setEventHandler(vec);
				this.unmarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			}
		} catch (JAXBException e) {
			logger.error("create jaxcontext instance error: ", e);
		} catch (SAXException e) {
			logger.error("create schema instance error: ", e);
		}
	}

	/**
	 * 
	 * Function：
	 * 			将pojo转换为xml, 如果异常则返回null
	 * @param obj
	 * 			将被转换的pojo
	 * @return
	 * 			转换后的xml字符串
	 * @version 1.0
	 */
	public String pojo2XMl(Object obj) {
		if (null == obj) {
			return null;
		}
		
		String ret = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			this.marshaller.marshal(obj, outputStream);
			byte[] bytes = outputStream.toByteArray();
			ret = new String(bytes, "UTF-8");
		} catch (JAXBException e) {
			logger.error("can not marshal obj: ", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("can not create string with utf-8: ", e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.warn("output stream can not close.");
			}
		}
		
		return ret;
	}

	/**
	 * 
	 * Function：
	 * 			将xml转换为pojo, 如果异常则返回null
	 * @param <T>
	 * 			泛型方法
	 * @param xml
	 * 			将要转换的xml内容
	 * @param clazz
	 * 			指定泛型类
	 * @return
	 * 			实体对象
	 * @version 1.0
	 */
	public <T> T XML2Pojo(String xml, Class<T> clazz) {
		if (null == xml || null == clazz) {
			return null;
		}

		Object obj = null;
		ByteArrayInputStream inputStream = null;
		try {
			byte[] bytes = xml.getBytes("UTF-8");
			inputStream = new ByteArrayInputStream(bytes);
			obj = this.unmarshaller.unmarshal(inputStream);
		} catch (JAXBException e) {
			logger.error("can not unmarshal xml: ", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("can not get bytes with utf-8: ", e);
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.warn("input stream can not close.");
				}
			}
		}
		
		return clazz.cast(obj);
	}

}
