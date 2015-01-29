package cn.dyz.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import cn.dyz.msg.base.Response;


/**
 * 消息解码器。将连续的字节按照协议规范分割成完整的消息包，并包装成ClientRequest。
 * @author dyz
 */
public class GameMsgDecoder extends CumulativeProtocolDecoder {

	public GameMsgDecoder() {
		
	}
	
	/**
	 * flag(1 byte)+length(4 byte,后边内容的长度)+protocol code(4 byte)+content
	 * length的长度包括  ：消息号+ 内容
	 */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer iobuffer,
			ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
		
		if(iobuffer.remaining()<(MsgProtocol.flagSize+MsgProtocol.lengthSize+MsgProtocol.msgCodeSize)){//数据不完整
			return false;
		}
		iobuffer.mark();
		byte flag = iobuffer.get();//flag，备用
		if (flag == 1) {
			int length = iobuffer.getInt();//读取长度字段
			if(length<=0 || length>MsgProtocol.maxPackLength){//长度字段异常
				return false;
			}
			if(iobuffer.remaining()>=length){//
				int preLimit = iobuffer.limit();//记录下当前的limit值
				iobuffer.limit(MsgProtocol.flagSize+MsgProtocol.lengthSize+length);
				byte[] body = new byte[length];
				iobuffer.get(body);
				iobuffer.limit(preLimit);
				Response message = new Response(body);
				protocolDecoderOutput.write(message);
				return true;
			}else{
				iobuffer.reset();
				return false;
			}
		}else{
			return false;
		}
	}
	
	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
	}

	public void dispose(IoSession session) throws Exception {
	}
}