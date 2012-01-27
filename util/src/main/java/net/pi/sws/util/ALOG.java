/*
 * Copyright (c) Ascelion Ltd. All rights reserved.
 *
 * ASCELION PROPRIETARY/CONFIDENTIAL.
 */

package net.pi.sws.util;


/**
 * @author PAPPY <a href="mailto:pa314159#64;gmail.com">&lt;pa314159#64;gmail.com&gt;</a>
 */
abstract class ALOG
{

	abstract boolean E( ExtLog.Level level );

	abstract void L( String source, ExtLog.Level level, String text, Throwable t );

	abstract void S( ExtLog.Level level );
}
