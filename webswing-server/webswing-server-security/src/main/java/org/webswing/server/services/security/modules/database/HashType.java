package org.webswing.server.services.security.modules.database;

import org.apache.shiro.crypto.hash.Md2Hash;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.Sha384Hash;
import org.apache.shiro.crypto.hash.Sha512Hash;

public enum HashType {
	NONE(null),
	MD2(Md2Hash.ALGORITHM_NAME),
	MD5(Md5Hash.ALGORITHM_NAME),
	SHA1(Sha1Hash.ALGORITHM_NAME),
	SHA256(Sha256Hash.ALGORITHM_NAME),
	SHA384(Sha384Hash.ALGORITHM_NAME),
	SHA512(Sha512Hash.ALGORITHM_NAME);

	private String hashName;

	private HashType(String hashName) {
		this.hashName = hashName;
	}

	public String getValue() {
		return hashName;
	}

}