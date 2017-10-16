package org.thingsboard.gateway.util.converter.transformer;

public abstract class AbstractDataValueTransformer implements DataValueTransformer {

    @Override
    public Double transformToDouble(String strValue) {
        throw new UnsupportedOperationException(String.format("%s doesn't support transforming to double value", this.getClass().getSimpleName()));
    }

    @Override
    public Long transformToLong(String strValue) {
        throw new UnsupportedOperationException(String.format("%s doesn't support transforming to long value", this.getClass().getSimpleName()));
    }

    @Override
    public String transformToString(String strValue) {
        throw new UnsupportedOperationException(String.format("%s doesn't support transforming to string value", this.getClass().getSimpleName()));
    }

    @Override
    public Boolean transformToBoolean(String strValue) {
        throw new UnsupportedOperationException(String.format("%s doesn't support transforming to boolean value", this.getClass().getSimpleName()));
    }

    @Override
    public boolean isApplicable(String strValue) {
        return true;
    }
}

