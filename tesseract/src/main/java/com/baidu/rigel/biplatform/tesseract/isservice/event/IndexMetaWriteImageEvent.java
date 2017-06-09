/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.isservice.event;

import org.springframework.context.ApplicationEvent;

import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;

/**
 * IndexMetaWriteImageEvent
 * @author lijin
 *
 */
public class IndexMetaWriteImageEvent extends ApplicationEvent {	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6272621722926037834L;
	/**
	 * IndexMeta
	 */
	private IndexMeta idxMeta;
	
	/**
	 * IndexMetaWriteImageEvent
	 * @param source
	 */
	public IndexMetaWriteImageEvent(Object source) {
		super(source);
		if(source instanceof IndexMeta){
			this.idxMeta=(IndexMeta) source; 
		}
		
	}
	
	
	/**
	 * @return the idxMeta
	 */
	public IndexMeta getIdxMeta() {
		return idxMeta;
	}


	/**
	 * @param idxMeta the idxMeta to set
	 */
	public void setIdxMeta(IndexMeta idxMeta) {
		this.idxMeta = idxMeta;
	}


	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
	public String toString() {
		return this.idxMeta==null?"null":this.idxMeta.toString();
	}


	/* (non-Javadoc)
	 * @see java.util.EventObject#getSource()
	 */
	@Override
	public IndexMeta getSource() {
		
		return this.idxMeta;
	}


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((idxMeta == null) ? 0 : idxMeta.hashCode ());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass () != obj.getClass ()) {
            return false;
        }
        IndexMetaWriteImageEvent other = (IndexMetaWriteImageEvent) obj;
        if (idxMeta == null) {
            if (other.idxMeta != null) {
                return false;
            }
        } else if (!idxMeta.equals (other.idxMeta)) {
            return false;
        }
        return true;
    }
	
	

}
