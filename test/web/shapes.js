/** A console output */
var Console = {};

Console.TRACE = 1;
Console.INFO = 2;

Console.outLevel = Console.INFO;

Console.out = function()
{
	var level = arguments[0];

	if( level < Console.outLevel ) {
		return;
	}

	var win = window.parent || window;
	var con = win.document.getElementById( 'console' );

	var text = "" + new Date() + " ";

	for( var k = 1; k < arguments.length; k++ ) {
		text += arguments[k];
	}

	if( con ) {
		con.appendChild( document.createTextNode( text ) );
		con.appendChild( document.createElement( 'BR' ) );

		con.scrollTop = con.scrollHeight - con.clientHeight;
	}
	else if( level >= Console.INFO ) {
		alert( text );
	}
};

Console.traceEvent = function( t, event )
{
	event = $( event );

	Console.out( Console.TRACE, "EVENT(", t, "): target =", event.target, ", targetId = ", event.target.id,
	        ", pointer = [", event.pointerX(), ", ", event.pointerY(), "]", ", " );
};

Console.init = function()
{
	var url = window.location.href;
	var params = url.toQueryParams();
	var level = params.console;

	if( level ) {
		level = level.toUpperCase();

		try {
			level = eval( Console[level] );

			if( level ) {
				this.outLevel = parseInt( level );
			}
		}
		catch( e ) {
			this.outLevel = Console.INFO;
		}
	}

	this.out( Console.INFO, "LEVEL is ", Console.outLevel );
};

/** Holds global variables */
var Global = {};

/** Initialize the application */

Global.init = function()
{
	Global.top = new Top( window.document );
};

/** Helper to investigate mouse events */
Global.bind = function( document, element )
{
	var o = new Base( document, element );

	o.bindMove( true );
	o.bindOver( true );
	o.bindDown( true );
	o.bindDone( true );
	o.bindGone( true );

	o.childElements().each( function( element )
	{
		Global.bind( document, element );
	} );
};

/** The top document */
Global.top = null;

/** Base class providing methods to bind mouse events */
var Base = Class.create( {
    document : null,

    initialize : function( document, target )
    {
	    this.document = $( document );

	    target = $( target );

	    Object.extend( target, this );

	    return target;
    },
    bindDown : function( set )
    {
	    if( set ) {
		    this.onmousedown = this.mouseDown.bindAsEventListener( this );
	    }
	    else {
		    this.onmousedown = null;
	    }
    },
    bindOver : function( set )
    {
	    if( set ) {
		    this.onmouseover = this.mouseOver.bindAsEventListener( this );
	    }
	    else {
		    this.onmouseover = null;
	    }
    },
    bindMove : function( set )
    {
	    if( set ) {
		    this.onmousemove = this.mouseMove.bindAsEventListener( this );
	    }
	    else {
		    this.onmousemove = null;
	    }
    },
    bindDone : function( set )
    {
	    if( set ) {
		    this.onmouseup = this.mouseDone.bindAsEventListener( this );
	    }
	    else {
		    this.onmouseup = null;
	    }
    },
    bindGone : function( set )
    {
	    if( set ) {
		    this.onmouseout = this.mouseGone.bindAsEventListener( this );
	    }
	    else {
		    this.onmouseout = null;
	    }
    },
    mouseOver : function( event )
    {
	    Console.traceEvent( "over", event );

	    return true;
    },
    mouseDown : function( event )
    {
	    Console.traceEvent( "down", event );

	    return true;
    },
    mouseMove : function( event )
    {
	    Console.traceEvent( "move", event );

	    return true;
    },
    mouseGone : function( event )
    {
	    Console.traceEvent( "gone", event );

	    return true;
    },
    mouseDone : function( event )
    {
	    Console.traceEvent( "done", event );

	    return true;
    },
} );

/** The top object bound to the BODY of the main document */
var Top = Class.create( Base, {
    source : null,
    panels : $A(),
    ajax : {
	    parameters : {}
    },

    initialize : function( $super, document )
    {
	    var obj = $super( document, document.body );

	    $$( '.frame' ).each( function( frame )
	    {
		    obj.panels.push( new Panel( frame, 5 ) );
	    } );

	    obj.bindDone();

	    return obj;
    },
    dragInit : function( source, event )
    {
	    event.stop();

	    this.source = source;

	    this.source.selected.each( function( shape )
	    {
		    shape.float( event );
	    } );

	    this.bindMove( true );
    },
    findPanel : function( event )
    {
	    var frame = $( event.target.frame );

	    if( !frame ) {
		    frame = event.target.ownerDocument.frame;
	    }

	    var offset = frame.cumulativeOffset();

	    offset.left += event.pointerX();
	    offset.top += event.pointerY();

	    Console.out( Console.TRACE, "frame = ", frame.id, ", offset = ", offset );

	    var panel = this.panels.detect( function( panel )
	    {
		    var f = $( panel.document.frame );
		    var o = f.cumulativeOffset();
		    var z = f.getDimensions();

		    if( (o.left < offset.left) && (offset.left < o.left + z.width) ) {
			    if( (o.top < offset.top) && (offset.top < o.top + z.height) ) {
				    return true;
			    }
		    }

		    return false;
	    } );

	    if( panel ) {
		    Console.out( Console.TRACE, "frame = " + panel.document.frame.id );
	    }

	    return {
	        panel : panel,
	        offset : offset
	    };
    },
    mouseMove : function( event, panel )
    {
	    event.stop();

	    var offset = null;

	    if( panel ) {
		    offset = $( panel.document.frame ).cumulativeOffset();
	    }

	    this.source.selected.each( function( shape )
	    {
		    shape.follow( event, offset );
	    } );
    },
    mouseDone : function( event )
    {
	    event.stop();

	    this.bindMove( false );

	    if( this.source ) {
		    var result = this.findPanel( event );

		    this.source.selected.each( function( shape )
		    {
			    shape.drop( result.panel, result.offset );
		    } );

		    if( result.panel ) {
			    this.ajax.parameters.shapes = this.source.selected.length;
			    this.ajax.parameters.target = result.panel.document.frame.id;
			    this.ajax.parameters.dropX = event.pointerX();
			    this.ajax.parameters.dropY = event.pointerY();

			    if( this.ajax.action ) {
				    new Ajax.Request( this.ajax.action, this.ajax );
			    }
		    }
		    
		    this.source = null;
	    }
    },
    bindMove : function( set )
    {
	    if( set ) {
		    this.document.onmousemove = this.mouseMove.bindAsEventListener( this );
	    }
	    else {
		    this.document.onmousemove = null;
	    }

	    this.panels.each( function( panel )
	    {
		    panel.bindMove( set );
	    } );
    },
    bindDone : function()
    {
	    this.document.onmouseup = this.mouseDone.bindAsEventListener( this );

	    this.panels.each( function( panel )
	    {
		    panel.document.onmouseup = this.document.onmouseup;
	    } );
    },
    setAjaxAction : function( action )
    {
	    this.ajax.action = action;
    },
    setAjaxCallback : function( callback )
    {
	    this.ajax.onSuccess = callback;
    }
} );

var Panel = Class.create( Base, {
    shapes : $A(),
    selected : $A(),

    initialize : function( $super, frame, count )
    {
	    var doc = frame.contentWindow.document;
	    var obj = $super( doc, doc.body );

	    doc.frame = frame;

	    obj.className = 'panel';
	    obj.id = frame.id + "_panel";

	    for( var k = 0; k < count; k++ ) {
		    obj.add( frame.id + "-" + (k + 1), 20, 4 + k * 24 );
	    }

	    return obj;
    },
    bindMove : function( set )
    {
	    if( set ) {
		    this.document.onmousemove = this.mouseMove.bindAsEventListener( this );
	    }
	    else {
		    this.document.onmousemove = null;
	    }
    },
    bindDone : function()
    {
	    this.document.onmouseup = this.mouseDone.bindAsEventListener( this );
    },
    mouseMove : function( event )
    {
	    Global.top.mouseMove( event, this );
    },
    add : function( text, x, y )
    {
	    var shape = new Moveable( this, text );

	    this.shapes.push( shape );

	    shape.moveTo( x, y );
	    
	    return shape;
    },
    remove : function( shape )
    {
	    var ix = this.shapes.indexOf( shape );

	    this.shapes[ix] = null;
	    this.shapes = this.shapes.compact();

	    this.removeChild( shape );
    },
    select : function( shape, control )
    {
	    if( control ) {
		    shape.selectNow( !shape.selected() );

		    var ix = this.selected.indexOf( shape );

		    if( ix < 0 ) {
			    this.selected.push( shape );
		    }
		    else {
			    this.selected[ix] = null;

			    this.selected = this.selected.compact();
		    }
	    }
	    else {
		    this.selected.each( function( shape )
		    {
			    shape.selectNow( false );
		    } );

		    this.selected = $A();

		    this.selected.push( shape );

		    shape.selectNow( true );
	    }
    },
} );

var Shape = Class.create( Base, {
    initialize : function( $super, parent, style, text )
    {
	    var obj = $super( parent.document, parent.document.createElement( 'div' ) );

	    obj.className = style;
	    obj.style.position = 'absolute';

	    parent.appendChild( obj );

	    obj.appendChild( this.document.createTextNode( text ) );

	    return obj;
    },
    moveTo : function( x, y )
    {
	    this.style.left = x + 'px';
	    this.style.top = y + 'px';
    },
} );

var Shadow = Class.create( Shape, {
    dx : null,
    dy : null,

    initialize : function( $super, text, dx, dy )
    {
	    var obj = $super( Global.top, 'shape_shadow', text );

	    obj.dx = dx;
	    obj.dy = dy;

	    return obj;
    },
} );

var Moveable = Class.create( Shape, {
    shadow : null,
    selectId : null,

    initialize : function( $super, panel, text )
    {
	    var obj = $super( panel, 'shape', text );

	    obj.bindDown( true );
	    obj.bindDone( true );

	    return obj;
    },
    mouseMove : function( event )
    {
	    event.stop();

	    window.clearTimeout( this.selectId );

	    this.bindMove( false );

	    if( this.selected() ) {
		    Global.top.dragInit( this.parentElement, event );
	    }
    },
    mouseDown : function( event )
    {
	    event.stop();

	    if( event.isLeftClick() ) {
		    this.bindMove( true );

		    this.selectId = Moveable._select.delay( 0.2, this, event.ctrlKey );
	    }
    },
    mouseDone : function( event )
    {
	    event.stop();

	    this.bindMove( false );
    },
    select : function( control )
    {
	    Console.out( Console.TRACE, "select" );

	    this.parentElement.select( this, control );
	    this.bindMove( false );
    },
    selectNow : function( set )
    {
	    if( set ) {
		    this.className = 'shape_selected';
	    }
	    else {
		    this.className = 'shape';
	    }
    },
    selected : function()
    {
	    return this.className == 'shape_selected';
    },
    float : function( event )
    {
	    var offset = this.cumulativeOffset();

	    var dx = event.pointerX() - offset.left;
	    var dy = event.pointerY() - offset.top;

	    Console.out( Console.TRACE, "dx = ", dx, ", dy = ", dy );

	    this.shadow = new Shadow( this.innerHTML, dx, dy );

	    this.follow( event );

	    this.hide();
    },
    follow : function( event, offset )
    {
	    var x = event.pointerX() - this.shadow.dx;
	    var y = event.pointerY() - this.shadow.dy;

	    if( offset ) {
		    x += offset.left;
		    y += offset.top;
	    }

	    this.shadow.moveTo( x, y );
    },
    drop : function( panel, offset )
    {
	    var x = offset.left - this.shadow.dx;
	    var y = offset.top - this.shadow.dy;

	    if( panel ) {
		    var o = panel.document.frame.cumulativeOffset();

		    x -= o.left;
		    y -= o.top;
	    }

	    Console.out( Console.TRACE, "x = ", x, ", y = ", y );

	    if( panel == this.parentElement ) {
		    this.moveTo( x, y );
		    this.show();
	    }
	    else if( panel ) {
		    panel.add( this.innerHTML, x, y ).selectNow();

		    this.parentElement.remove( this );
	    }
	    else {
	    	this.show();
	    }

	    Global.top.removeChild( this.shadow );

	    this.shadow = null;
    }
} );

Moveable._select = function(shape, control) {
	shape.select(control);
};

