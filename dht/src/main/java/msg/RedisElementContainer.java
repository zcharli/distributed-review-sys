package msg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by cli on 9/26/2016.
 */
public class RedisElementContainer {
    public byte[] buffer;
    public int[] locationBuffer;
    public int[] domainBuffer;
    public int[] contentBuffer;
    public int[] versionBuffer;

    public RedisElementContainer() {}
    public RedisElementContainer(byte[] data) {
        this.buffer = data;
    }

    @JsonSerialize(using=ByteArraySerializer.class)
    public byte[] getBuffer() {
        return buffer;
    }
    public void setBuffer(byte[] d) {
        this.buffer = d;
    }

    public void setContentBuffer(int[] contentBuffer) {
        this.contentBuffer = contentBuffer;
    }

    public void setDomainBuffer(int[] domainBuffer) {
        this.domainBuffer = domainBuffer;
    }

    public void setLocationBuffer(int[] locationBuffer) {
        this.locationBuffer = locationBuffer;
    }

    public void setVersionBuffer(int[] versionBuffer) {
        this.versionBuffer = versionBuffer;
    }

    public int[] getDomainBuffer() {
        return domainBuffer;
    }

    public int[] getLocationBuffer() {
        return locationBuffer;
    }

    public int[] getVersionBuffer() {
        return versionBuffer;
    }

    public int[] getContentBuffer() {
        return contentBuffer;
    }

    @JsonIgnore
    public static RedisElementContainerBuilder builder() {
        return new RedisElementContainerBuilder();
    }

    public static class RedisElementContainerBuilder {
        private int[] locationBuffer;
        private int[] domainBuffer;
        private int[] contentBuffer;
        private int[] versionBuffer;
        private byte[] buffer;

        public RedisElementContainerBuilder() {}

        public RedisElementContainerBuilder setBuffer(byte[] d) {
            this.buffer = d;
            return this;
        }

        public RedisElementContainerBuilder setContentBuffer(int[] contentBuffer) {
            this.contentBuffer = contentBuffer;
            return this;
        }

        public RedisElementContainerBuilder setDomainBuffer(int[] domainBuffer) {
            this.domainBuffer = domainBuffer;
            return this;
        }

        public RedisElementContainerBuilder setLocationBuffer(int[] locationBuffer) {
            this.locationBuffer = locationBuffer;
            return this;
        }

        public RedisElementContainerBuilder setVersionBuffer(int[] versionBuffer) {
            this.versionBuffer = versionBuffer;
            return this;
        }

        public RedisElementContainer build() {
            RedisElementContainer ret = new RedisElementContainer();
            ret.setBuffer(buffer);
            ret.setContentBuffer(contentBuffer);
            ret.setDomainBuffer(domainBuffer);
            ret.setLocationBuffer(locationBuffer);
            ret.setVersionBuffer(versionBuffer);
            return ret;
        }
    }
}
