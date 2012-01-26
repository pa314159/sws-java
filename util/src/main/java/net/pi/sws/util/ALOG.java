/*
 * Copyright (c) Ascelion Ltd. All rights reserved.
 *
 * ASCELION PROPRIETARY/CONFIDENTIAL.
 */

package net.pi.sws.util;


/**
 * @author Pappy Răzvan STĂNESCU <a href="mailto:pappy&#64;ascelion.com">&lt;pappy&#64;ascelion.com&gt;</a>
 */
abstract class ALOG
{

	abstract boolean E( ExtLog.Level level );

	abstract void L( String source, ExtLog.Level level, String text, Throwable t );

	abstract void S( ExtLog.Level level );
}
