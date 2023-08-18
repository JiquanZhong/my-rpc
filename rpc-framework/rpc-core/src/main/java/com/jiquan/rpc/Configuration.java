package com.jiquan.rpc;

import com.jiquan.IdGenerator;
import com.jiquan.rpc.compress.Compressor;
import com.jiquan.rpc.compress.impl.GzipCompressor;
import com.jiquan.rpc.discovery.RegistryConfig;
import com.jiquan.rpc.loadbalance.LoadBalancer;
import com.jiquan.rpc.loadbalance.impl.RoundRobinLoadBalancer;
import com.jiquan.rpc.serialize.Serializer;
import com.jiquan.rpc.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 全局的配置类，代码配置-->xml配置-->默认项
 *
 * @author it楠老师
 * @createTime 2023-07-11
 */
@Data
@Slf4j
public class Configuration {

	// 配置信息-->端口号
	private int port = 8094;

	// 配置信息-->应用程序的名字
	private String appName = "default";

	// 配置信息-->注册中心
	private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

	// 配置信息-->序列化协议
	private ProtocolConfig protocolConfig = new ProtocolConfig("jdk");

	// 配置信息-->序列化协议
	private String serializeType = "jdk";
	private Serializer serializer = new JdkSerializer();

	// 配置信息-->压缩使用的协议
	private String compressType = "gzip";
	private Compressor compressor = new GzipCompressor();

	// 配置信息-->id发射器
	public IdGenerator idGenerator = new IdGenerator(1, 2);

	// 配置信息-->负载均衡策略
	private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

	// 读xml，dom4j
	public Configuration() {
		// 读取xml获得上边的信息
		loadFromXml(this);

	}

	/**
	 * 从配置文件读取配置信息,我们不使用dom4j，使用原生的api
	 * @param configuration 配置实例
	 */
	private void loadFromXml(Configuration configuration) {
		try {
			// 1、创建一个document
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("rpc.xml");
			Document doc = builder.parse(inputStream);

			// 2、获取一个xpath解析器
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();

			configuration.setPort(resolvePort(doc, xpath));
			configuration.setAppName(resolveAppName(doc, xpath));

			configuration.setIdGenerator(resolveIdGenerator(doc, xpath));

			configuration.setRegistryConfig(resolveRegistryConfig(doc, xpath));

			configuration.setCompressType(resolveCompressType(doc, xpath));
			configuration.setCompressor(resolveCompressCompressor(doc, xpath));

			configuration.setSerializeType(resolveSerializeType(doc, xpath));
			configuration.setProtocolConfig(new ProtocolConfig(this.serializeType));

			configuration.setSerializer(resolveSerializer(doc, xpath));

			configuration.setLoadBalancer(resolveLoadBalancer(doc, xpath));


		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.info("If no configuration file is found or an exception occurs when parsing the configuration file, " +
							 "the default configuration is used.", e);
		}
	}


	/**
	 * 解析端口号
	 * @param doc   document
	 * @param xpath  xpath parser
	 * @return port
	 */
	private int resolvePort(Document doc, XPath xpath) {
		String expression = "/configuration/port";
		String portString = parseString(doc, xpath, expression);
		return Integer.parseInt(portString);
	}

	/**
	 * 解析应用名称
	 * @param doc    document
	 * @param xpath  xpath parser
	 * @return       app name
	 */
	private String resolveAppName(Document doc, XPath xpath) {
		String expression = "/configuration/appName";
		return parseString(doc, xpath, expression);
	}

	/**
	 * 解析负载均衡器
	 * @param doc    document
	 * @param xpath  xpath
	 * @return       loadBalancer
	 */
	private LoadBalancer resolveLoadBalancer(Document doc, XPath xpath) {
		String expression = "/configuration/loadBalancer";
		return parseObject(doc, xpath, expression, null);
	}

	/**
	 * 解析id发号器
	 * @param doc    document
	 * @param xpath  xpath
	 * @return id generator
	 */
	private IdGenerator resolveIdGenerator(Document doc, XPath xpath) {
		String expression = "/configuration/idGenerator";
		String aClass = parseString(doc, xpath, expression, "class");
		String dataCenterId = parseString(doc, xpath, expression, "dataCenterId");
		String machineId = parseString(doc, xpath, expression, "MachineId");

		try {
			Class<?> clazz = Class.forName(aClass);
			Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
					.newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
			return (IdGenerator) instance;
		} catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
				 NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * parse registry
	 * @param doc    document
	 * @param xpath  xpath
	 * @return       RegistryConfig
	 */
	private RegistryConfig resolveRegistryConfig(Document doc, XPath xpath) {
		String expression = "/configuration/registry";
		String url = parseString(doc, xpath, expression, "url");
		return new RegistryConfig(url);
	}


	/**
	 * compressor
	 * @param doc    document
	 * @param xpath  xpath
	 * @return       Compressor
	 */
	private Compressor resolveCompressCompressor(Document doc, XPath xpath) {
		String expression = "/configuration/compressor";
		return parseObject(doc, xpath, expression, null);
	}

	/**
	 *compress algorithm
	 * @param doc    document
	 * @param xpath  xpath
	 * @return       algorithm name
	 */
	private String resolveCompressType(Document doc, XPath xpath) {
		String expression = "/configuration/compressType";
		return parseString(doc, xpath, expression, "type");
	}

	/**
	 * serializeType
	 * @param doc
	 * @param xpath  xpath
	 * @return       serializeType
	 */
	private String resolveSerializeType(Document doc, XPath xpath) {
		String expression = "/configuration/serializeType";
		return parseString(doc, xpath, expression, "type");
	}

	/**
	 * 解析序列化器
	 * @param doc    文档
	 * @param xpath  xpath解析器
	 * @return       序列化器
	 */
	private Serializer resolveSerializer(Document doc, XPath xpath) {
		String expression = "/configuration/serializer";
		return parseObject(doc, xpath, expression, null);
	}


	/**
	 * port   <port>7777</>
	 * @param doc        document
	 * @param xpath      xpath
	 * @param expression xpath
	 * @return value of port
	 */
	private String parseString(Document doc, XPath xpath, String expression) {
		try {
			XPathExpression expr = xpath.compile(expression);
			// 我们的表达式可以帮我们获取节点
			Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
			return targetNode.getTextContent();
		} catch (XPathExpressionException e) {
			log.error("An exception occurred while parsing the expression.", e);
		}
		return null;
	}

	private String parseString(Document doc, XPath xpath, String expression, String AttributeName) {
		try {
			XPathExpression expr = xpath.compile(expression);
			Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
			return targetNode.getAttributes().getNamedItem(AttributeName).getNodeValue();
		} catch (XPathExpressionException e) {
			log.error("An exception occurred while parsing the expression.", e);
		}
		return null;
	}

	private <T> T parseObject(Document doc, XPath xpath, String expression, Class<?>[] paramType, Object... param) {
		try {
			XPathExpression expr = xpath.compile(expression);
			Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
			String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
			Class<?> aClass = Class.forName(className);
			Object instant = null;
			if (paramType == null) {
				instant = aClass.getConstructor().newInstance();
			} else {
				instant = aClass.getConstructor(paramType).newInstance(param);
			}
			return (T) instant;
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
				 InvocationTargetException | XPathExpressionException e) {
			log.error("An exception occurred while parsing the expression.", e);
		}
		return null;
	}

	public static void main(String[] args) {
		Configuration configuration = new Configuration();
	}
}
