package org.simplejavamail.api.internal.smimesupport.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simplejavamail.api.email.OriginalSmimeDetails;

public class PlainSmimeDetails implements OriginalSmimeDetails {

	private static final long serialVersionUID = 1234567L;

	@NotNull
	@Override
	public SmimeMode getSmimeMode() {
		return SmimeMode.PLAIN;
	}

	@Override
	public String toString() {
		return "PlainSmimeDetails{}";
	}

	@Override
	public int hashCode() {
		return PlainSmimeDetails.class.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return obj != null && obj.getClass() == getClass();
	}

	@Nullable
	@Override
	public String getSmimeMime() {
		return null;
	}

	@Nullable
	@Override
	public String getSmimeType() {
		return null;
	}

	@Nullable
	@Override
	public String getSmimeName() {
		return null;
	}

	@Nullable
	@Override
	public String getSmimeProtocol() {
		return null;
	}

	@Nullable
	@Override
	public String getSmimeMicalg() {
		return null;
	}

	@Nullable
	@Override
	public String getSmimeSignedBy() {
		return null;
	}

	@Nullable
	@Override
	public Boolean getSmimeSignatureValid() {
		return null;
	}
}