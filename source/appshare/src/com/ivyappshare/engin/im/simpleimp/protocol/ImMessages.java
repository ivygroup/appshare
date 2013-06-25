package com.ivyappshare.engin.im.simpleimp.protocol;


public class ImMessages {
	/*  IP Messenger Communication Protocol version 3.0 define  */

	/*  macro  */
	// public static final long IPMSG_COMMASK = 0x000000ffL;
	// public static final long IPMSG_OPTMASK = 0xffffff00L;
	public static long GET_MODE(long command) {
		return command & 0x000000ffL;
	}
	public static long GET_OPT(long command) {
		return command & 0xffffff00L;
	}

	/*  header  */
	public static final String IPMSG_VERSION = "1_ivy_2"; // version 2
	public static final int IPMSG_DEFAULT_PORT =	0x0979;
	public static final String DEFAULT_ENCODE = "UTF-8";
	public static final int DEFAULT_BUFFER_LENGTH = 1024;

	/*  command  */
	public static final long IPMSG_NOOPERATION =	0x00000000L;

	public static final long IPMSG_BR_ENTRY =		0x00000001L;
	public static final long IPMSG_BR_EXIT =		0x00000002L;
	public static final long IPMSG_ANSENTRY =		0x00000003L;
	public static final long IPMSG_BR_ABSENCE =		0x00000004L;

	public static final long IPMSG_BR_ISGETLIST =	0x00000010L;
	public static final long IPMSG_OKGETLIST =		0x00000011L;
	public static final long IPMSG_GETLIST =		0x00000012L;
	public static final long IPMSG_ANSLIST =		0x00000013L;
	public static final long IPMSG_BR_ISGETLIST2 =	0x00000018L;

	public static final long IPMSG_SENDMSG =		0x00000020L;
	public static final long IPMSG_RECVMSG =		0x00000021L;
	public static final long IPMSG_READMSG =		0x00000030L;
	public static final long IPMSG_DELMSG =			0x00000031L;
	public static final long IPMSG_ANSREADMSG =		0x00000032L;

	public static final long IPMSG_GETINFO =		0x00000040L;
	public static final long IPMSG_SENDINFO =		0x00000041L;

	public static final long IPMSG_GETABSENCEINFO =	0x00000050L;
	public static final long IPMSG_SENDABSENCEINFO =0x00000051L;

	public static final long IPMSG_GETFILEDATA =	0x00000060L;
	public static final long IPMSG_RELEASEFILES =	0x00000061L;
	public static final long IPMSG_GETDIRFILES =	0x00000062L;

	public static final long IPMSG_GETPUBKEY =		0x00000072L;
	public static final long IPMSG_ANSPUBKEY =		0x00000073L;

	/*  option for all command  */
	public static final long IPMSG_ABSENCEOPT =		0x00000100L;
	public static final long IPMSG_SERVEROPT =		0x00000200L;
	public static final long IPMSG_DIALUPOPT =		0x00010000L;
	public static final long IPMSG_FILEATTACHOPT =	0x00200000L; // 2097152
	public static final long IPMSG_ENCRYPTOPT =		0x00400000L;
	public static final long IPMSG_ENCRYPTOPTOLD =	0x00800000L;
	public static final long IPMSG_CAPUTF8OPT =		0x01000000L;
	public static final long IPMSG_ENCEXTMSGOPT =	0x04000000L;
	public static final long IPMSG_CLIPBOARDOPT =	0x08000000L;

	/*  option for send command  */
	public static final long IPMSG_SENDCHECKOPT =	0x00000100L;
	public static final long IPMSG_SECRETOPT =		0x00000200L;
	public static final long IPMSG_BROADCASTOPT =	0x00000400L;
	public static final long IPMSG_MULTICASTOPT =	0x00000800L;
	public static final long IPMSG_AUTORETOPT =		0x00002000L;
	public static final long IPMSG_RETRYOPT =		0x00004000L;
	public static final long IPMSG_PASSWORDOPT =	0x00008000L;
	public static final long IPMSG_NOLOGOPT =		0x00020000L;
	public static final long IPMSG_NOADDLISTOPT	=	0x00080000L;
	public static final long IPMSG_READCHECKOPT =	0x00100000L;
	public static final long IPMSG_SECRETEXOPT = (IPMSG_READCHECKOPT|IPMSG_SECRETOPT);

	/*  obsolete option for send command  */
	public static final long IPMSG_NOPOPUPOPT =		0x00001000L;
	public static final long IPMSG_NEWMUTIOPT =		0x00040000L;

	/* encryption/capability flags for encrypt command */
	public static final long IPMSG_RSA_512 =		0x00000001L;
	public static final long IPMSG_RSA_1024 =		0x00000002L;
	public static final long IPMSG_RSA_2048 =		0x00000004L;
	public static final long IPMSG_RC2_40 =			0x00001000L;
	public static final long IPMSG_BLOWFISH_128 =	0x00020000L;
	public static final long IPMSG_AES_256 =		0x00100000L;
	public static final long IPMSG_PACKETNO_IV =	0x00800000L;
	public static final long IPMSG_ENCODE_BASE64 =	0x01000000L;
	public static final long IPMSG_SIGN_SHA1 =		0x20000000L;

	/* compatibilty for Win beta version */
	public static final long IPMSG_RC2_40OLD =			0x00000010L;	// for beta1-4 only
	public static final long IPMSG_RC2_128OLD =			0x00000040L;	// for beta1-4 only
	public static final long IPMSG_BLOWFISH_128OLD =	0x00000400L;	// for beta1-4 only
	public static final long IPMSG_RC2_128OBSOLETE =	0x00004000L;
	public static final long IPMSG_RC2_256OBSOLETE =	0x00008000L;
	public static final long IPMSG_BLOWFISH_256OBSOL =	0x00040000L;
	public static final long IPMSG_AES_128OBSOLETE =	0x00080000L;
	public static final long IPMSG_SIGN_MD5OBSOLETE =	0x10000000L;
	public static final long IPMSG_UNAMEEXTOPTOBSOLT =	0x02000000L;

	/* file types for fileattach command */        // Low 8 bits
	public static final long IPMSG_FILE_REGULAR =		0x00000001L;
	public static final long IPMSG_FILE_DIR =			0x00000002L;
	public static final long IPMSG_FILE_RETPARENT= 		0x00000003L;	// return parent directory
	public static final long IPMSG_FILE_SYMLINK =		0x00000004L;
	public static final long IPMSG_FILE_CDEV =			0x00000005L;	// for UNIX
	public static final long IPMSG_FILE_BDEV =			0x00000006L;	// for UNIX
	public static final long IPMSG_FILE_FIFO =			0x00000007L;	// for UNIX
	public static final long IPMSG_FILE_RESFORK =		0x00000010L;	// for Mac
	public static final long IPMSG_FILE_CLIPBOARD =		0x00000020L;	// for Windows Clipboard

	/* file attribute options for fileattach command */    // High 24 bits
	public static final long IPMSG_FILE_RONLYOPT =		0x00000100L;
	public static final long IPMSG_FILE_HIDDENOPT =		0x00001000L;
	public static final long IPMSG_FILE_EXHIDDENOPT =	0x00002000L;	// for MacOS X
	public static final long IPMSG_FILE_ARCHIVEOPT =	0x00004000L;
	public static final long IPMSG_FILE_SYSTEMOPT =		0x00008000L;

	/* extend attribute types for fileattach command */
	public static final long IPMSG_FILE_UID =			0x00000001L;
	public static final long IPMSG_FILE_USERNAME =		0x00000002L;	// uid by string
	public static final long IPMSG_FILE_GID =			0x00000003L;
	public static final long IPMSG_FILE_GROUPNAME =		0x00000004L;	// gid by string
	public static final long IPMSG_FILE_CLIPBOARDPOS =	0x00000008L;	//
	public static final long IPMSG_FILE_PERM =			0x00000010L;	// for UNIX
	public static final long IPMSG_FILE_MAJORNO =		0x00000011L;	// for UNIX devfile
	public static final long IPMSG_FILE_MINORNO =		0x00000012L;	// for UNIX devfile
	public static final long IPMSG_FILE_CTIME =			0x00000013L;	// for UNIX
	public static final long IPMSG_FILE_MTIME =			0x00000014L;
	public static final long IPMSG_FILE_ATIME =			0x00000015L;
	public static final long IPMSG_FILE_CREATETIME =	0x00000016L;
	public static final long IPMSG_FILE_CREATOR =		0x00000020L;	// for Mac
	public static final long IPMSG_FILE_FILETYPE =		0x00000021L;	// for Mac
	public static final long IPMSG_FILE_FINDERINFO =	0x00000022L;	// for Mac
	public static final long IPMSG_FILE_ACL =			0x00000030L;
	public static final long IPMSG_FILE_ALIASFNAME =	0x00000040L;	// alias fname

	/*  end of IP Messenger Communication Protocol version 3.0 define  */


	/* begin ivy_1 protocol define */
	// The file has two part:
	//     attribute:
	//         file type: low 8 bits
	//         file option: high 24 bits.
	//     extend-attribute:
	//         only some class, and the value may be string which defined by user.
	// The file's command:
	//     fileID:filename:size:mtime:fileattr[:extend-attr=val1[,val2...][:extend-attr2=...]]:\a:fileID2...
	// The ivy's extend file type is: we only add a extend-attribute to the IPMSG.
	//     The file type value using hex string.
	public static final long IVY_FILE_TYPE_ATTR        =   0x00000100L;

	public static final long IVY_FILE_TYPE_VAL_APP     =   0x00000001L;
	public static final long IVY_FILE_TYPE_VAL_CONTACT =   0x00000002L;
	public static final long IVY_FILE_TYPE_VAL_PICTURE =   0x00000003L;
	public static final long IVY_FILE_TYPE_VAL_MUSIC   =   0x00000004L;
	public static final long IVY_FILE_TYPE_VAL_VIDEO   =   0x00000005L;
	public static final long IVY_FILE_TYPE_VAL_OTHERFILE   =   0x00000006L;
	public static final long IVY_FILE_TYPE_VAL_RECORD  =   0x00000007L;

	// extend command. because those command is copy from iptux, so the name is IPTUX_ prefix.
	public static final long IPTUX_SENDICON            =   0x000000FEL;
	//                 the two command, is trans headicon by TCP.  IPTUX_SENDICON trans icon by UDP.  Now, we use TCP to trans it.
	public static final long IVY_SENDICON_NOTIFY       =   0x000000F1L;
	public static final long IVY_GETHEADICON           =   0x000000F2L;

	// public static final long IVY_KEEPALIVE_ASK         =   0x000000F5L;
	// public static final long IVY_KEEPALIVE_ANS         =   0x000000F6L;


	/* end ivy_1 protocol define */


	/* begin ivy_2 protocol define */
	public static final long IVY_GROUP_CHAT_ATTR        =   0x00000200L;
	public static final long IVY_GROUP_CHAT_VAL_TRUE    =   0x00000001L;
	public static final long IVY_GROUP_CHAT_VAL_FALSE   =   0x00000002L;

	/* end ivy_2 protocol define */
}
