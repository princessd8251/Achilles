package mapping.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * UserBean
 * 
 * @author DuyHai DOAN
 * 
 */
@Table
public class UserBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	@Id
	private Long userId;

	@Column
	private String name;

	public Long getUserId()
	{
		return userId;
	}

	public void setUserId(Long userId)
	{
		this.userId = userId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
