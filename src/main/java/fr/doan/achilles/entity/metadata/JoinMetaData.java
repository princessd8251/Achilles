package fr.doan.achilles.entity.metadata;

import java.lang.reflect.Method;

import me.prettyprint.hector.api.Serializer;

/**
 * JoinMetaData
 * 
 * @author DuyHai DOAN
 * 
 */
public class JoinMetaData<K, V>
{

	private Class<K> idClass;
	private Serializer<K> idSerializer;
	private Method idGetter;
	private EntityMeta<K> entityMeta;

	public JoinMetaData() {}

	public JoinMetaData(Class<K> idClass) {
		this.idClass = idClass;
	}

	public Class<K> getIdClass()
	{
		return idClass;
	}

	public void setIdClass(Class<K> idClass)
	{
		this.idClass = idClass;
	}

	public Serializer<K> getIdSerializer()
	{
		return idSerializer;
	}

	public void setIdSerializer(Serializer<K> idSerializer)
	{
		this.idSerializer = idSerializer;
	}

	public Method getIdGetter()
	{
		return idGetter;
	}

	public void setIdGetter(Method idGetter)
	{
		this.idGetter = idGetter;
	}

	public EntityMeta<K> getEntityMeta()
	{
		return entityMeta;
	}

	public void setEntityMeta(EntityMeta<K> entityMeta)
	{
		this.entityMeta = entityMeta;
	}
}
