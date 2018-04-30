package org.webswing.server.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webswing.model.MsgOut;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.ProtocolMessageEnum;

public class ProtoMapper {
	public static final String basicProtoPackage = "org.webswing.server.model.proto.Webswing";
	private Map<Class<?>, Class<?>> classProtoMap = new HashMap<Class<?>, Class<?>>();

	public byte[] encodeProto(MsgOut msg) throws IOException {
		Message result = buildMessage(msg);
		return result.toByteArray();
	}

	public <T> T decodeProto(byte[] bytes, Class<T> msgInClass) throws IOException {
		T result = decodeMessage(bytes, msgInClass);
		return result;
	}

	private <T> T decodeMessage(byte[] bytes, Class<T> c) throws IOException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			Class<?> protoClass = resolveProtoCounterpartClass(c);
			GeneratedMessage protoMsg = (GeneratedMessage) protoClass.getDeclaredMethod("parseFrom", byte[].class).invoke(null, bytes);
			return decodeMessage(protoMsg, c);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("decoding " + c.getClass() + " from ProtoBuffer format failed!", e);
		}

	}

	@SuppressWarnings("rawtypes")
	private <T> T decodeMessage(Message protoMsg, Class<T> c) throws IOException {
		try {
			T result = c.newInstance();
			Map<FieldDescriptor, Object> valueMap = protoMsg.getAllFields();
			for (FieldDescriptor fd : valueMap.keySet()) {
				Field field = c.getDeclaredField(fd.getName());
				field.setAccessible(true);
				if (fd.isRepeated()) {
					if (field.getType() == List.class && field.getGenericType() instanceof ParameterizedType) {
						List<Object> decodedList = new ArrayList<Object>();
						ParameterizedType type = (ParameterizedType) field.getGenericType();
						Class<?> param = (Class<?>) type.getActualTypeArguments()[0];
						for (Object o : (List) valueMap.get(fd)) {
							if (fd.getJavaType().equals(JavaType.MESSAGE)) {
								decodedList.add(decodeMessage((Message) o, param));
							} else if (fd.getJavaType().equals(JavaType.ENUM)) {
								Enum<?> value = resolveEnum((EnumValueDescriptor) o, param);
								decodedList.add(value);
							} else if (fd.getJavaType().equals(JavaType.BYTE_STRING)) {
								byte[] value = ((ByteString) o).toByteArray();
								decodedList.add(value);
							} else {
								decodedList.add(o);
							}
						}
						field.set(result, decodedList);
					} else {
						throw new IOException("Field '" + fd.getName() + "' of " + c + " has to be List with generics type!");
					}
				} else {
					if (fd.getJavaType().equals(JavaType.MESSAGE)) {
						Object o = decodeMessage((Message) valueMap.get(fd), field.getType());
						field.set(result, o);
					} else if (fd.getJavaType().equals(JavaType.ENUM)) {
						Enum<?> value = resolveEnum((EnumValueDescriptor) valueMap.get(fd), field.getType());
						field.set(result, value);
					} else if (fd.getJavaType().equals(JavaType.BYTE_STRING)) {
						byte[] value = ((ByteString) valueMap.get(fd)).toByteArray();
						field.set(result, value);
					} else {
						field.set(result, valueMap.get(fd));
					}
				}
			}
			return result;
		} catch (Exception e) {
			throw new IOException("decoding " + c.getClass() + " from ProtoBuffer format failed!", e);
		}
	}

	private Message buildMessage(Object msg) throws IOException {
		if (msg == null) {
			return null;
		}
		try {
			Class<?> c = msg.getClass();
			Class<?> protoClass = resolveProtoCounterpartClass(c);
			Builder b = null;
			try {
				b = (Builder) protoClass.getDeclaredMethod("newBuilder").invoke(null);
			} catch (Exception e) {
				throw new IOException("Not able to resolve ProtoBuffer builder for " + protoClass.getName() + ".", e);
			}
			if (b != null) {
				Field[] fields = c.getDeclaredFields();
				for (Field field : fields) {
					field.setAccessible(true);
					Object value = field.get(msg);
					if (Modifier.isStatic(field.getModifiers()) || value == null) {
						continue;
					}
					String fieldName = field.getName();
					FieldDescriptor protoField = b.getDescriptorForType().findFieldByName(fieldName);
					if (protoField == null) {
						throw new IOException("Field '" + fieldName + "' not found in " + protoClass);
					}
					if (Collection.class.isAssignableFrom(field.getType())) {
						Collection<?> list = (Collection<?>) value;
						for (Object val : list) {
							resolveAndSetMessageValue(b, protoField, val, true);
						}
					} else {
						resolveAndSetMessageValue(b, protoField, value, false);
					}
				}
			}
			return b.build();
		} catch (Exception e) {
			throw new IOException("encoding " + msg.getClass() + " to ProtoBuffer format failed!", e);
		}
	}

	private Enum<?> resolveEnum(EnumValueDescriptor pe, Class<?> type) throws IOException {
		if (pe == null) {
			return null;
		}
		try {
			List<?> enumCons = Arrays.asList(type.getEnumConstants());
			for (Object e : enumCons) {
				Enum<?> enm = (Enum<?>) e;
				if (pe.getName().equals(enm.name())) {
					return enm;
				}
			}
		} catch (Exception e) {
			throw new IOException("Resolving enum " + type + " from ProtoBuffer format failed!", e);
		}
		return null;
	}

	private EnumValueDescriptor resolveEnum(Enum<?> enm) throws IOException {
		if (enm == null) {
			return null;
		}
		try {
			Class<?> protoClass = resolveProtoCounterpartClass(enm.getClass());
			if (protoClass.isEnum()) {
				List<?> protoEnumCons = Arrays.asList(protoClass.getEnumConstants());
				for (Object pe : protoEnumCons) {
					ProtocolMessageEnum penm = (ProtocolMessageEnum) pe;
					if (penm.getValueDescriptor().getName().equals(enm.name())) {
						return penm.getValueDescriptor();
					}
				}
			}
			throw new IOException("Could not resolve proto counterpart for " + enm.getClass() + "." + enm);
		} catch (Exception e) {
			throw new IOException("resolving Enum " + enm.getClass() + " to ProtoBuffer format failed!", e);
		}
	}

	private Class<?> resolveProtoCounterpartClass(Class<?> c) throws ClassNotFoundException {
		if (!classProtoMap.containsKey(c)) {
			ClassLoader cl = c.getClassLoader();
			String className = c.getName().substring(c.getName().lastIndexOf(".") + 1);
			String protoClassName = basicProtoPackage + "$" + (className.replaceAll("\\$", "Proto\\$")) + "Proto";
			Class<?> protoClass = cl.loadClass(protoClassName);
			classProtoMap.put(c, protoClass);
		}
		Class<?> protoClass = classProtoMap.get(c);
		return protoClass;
	}

	private void resolveAndSetMessageValue(Builder b, FieldDescriptor protoField, Object value, boolean list) throws IOException {
		if (protoField.getJavaType().equals(JavaType.MESSAGE)) {
			setField(b, protoField, buildMessage(value), list);
		} else if (protoField.getJavaType().equals(JavaType.ENUM)) {
			setField(b, protoField, resolveEnum((Enum<?>) value), list);
		} else if (protoField.getJavaType().equals(JavaType.BYTE_STRING)) {
			setField(b, protoField, ByteString.readFrom(new ByteArrayInputStream((byte[]) value)), list);
		} else {
			setField(b, protoField, value, list);
		}
	}

	private void setField(Builder b, FieldDescriptor protoField, Object message, boolean list) throws IOException {
		try {
			if (list) {
				b.addRepeatedField(protoField, message);
			} else {
				b.setField(protoField, message);
			}
		} catch (Exception e) {
			throw new IOException("Could not set " + message.getClass() + " to " + protoField.getFullName(), e);
		}
	}

}
