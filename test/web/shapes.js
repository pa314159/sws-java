/** Holds global variables */
var Global = {};

/** A trace function used for debugging */
Global.trace = function() {
	var win = window.parent || window;
	var con = win.document.getElementById('console');

	if (con) {
		var text = '' + new Date() + " ";

		for ( var k = 0; k < arguments.length; k++) {
			text += arguments[k];
		}

		// console.innerHTML = text;
		con.appendChild(document.createTextNode(text));
		con.appendChild(document.createElement('BR'));

		con.scrollTop = con.scrollHeight - con.clientHeight;
	}
};

/** Initialize the application */
Global.init = function() {
	Global.top = new Top(window.document);
};

/** The top document */
Global.top = null;

/** Base class providing methods to bind mouse events */
var Base = Class.create({
	document : null,

	initialize : function(document, target) {
		this.document = $(document);

		target = $(target);

		Object.extend(target, this);

		return target;
	},
	bindDown : function(set) {
		if (set) {
			this.onmousedown = this.mouseDown.bindAsEventListener(this);
		} else {
			this.onmousedown = null;
		}
	},
	bindOver : function(set) {
		if (set) {
			this.onmouseover = this.mouseOver.bindAsEventListener(this);
		} else {
			this.onmouseover = null;
		}
	},
	bindMove : function(set) {
		if (set) {
			this.onmousemove = this.mouseMove.bindAsEventListener(this);
		} else {
			this.onmousemove = null;
		}
	},
	bindDone : function(set) {
		if (set) {
			this.onmouseup = this.mouseDone.bindAsEventListener(this);
		} else {
			this.onmouseup = null;
		}
	},
	mouseOver : function(event) {
		event.stop();
	},
	mouseDown : function(event) {
		event.stop();
	},
	mouseMove : function(event) {
		event.stop();
	},
	mouseDone : function(event) {
		event.stop();
	},
});

/** The top object bound to the BODY of the main document */
var Top = Class.create(Base, {
	source : null,
	panels : $A(),

	initialize : function($super, document) {
		var obj = $super(document, document.body);

		$$('.frame').each(function(frame) {
			obj.panels.push(new Panel(frame, 5));
		});

		obj.bindDone();

		return obj;
	},
	dragInit : function(source, event) {
		event.stop();

		this.source = source;

		this.source.selected.each(function(shape) {
			shape.float(event);
		});

		this.bindMove(true);
	},
	findPanel: function(event) {
		var frame = $(event.target.frame);

		if( !frame ) {
			frame = event.target.ownerDocument.frame;

//			Global.trace("NULL");
		}
		
		var offset = frame.cumulativeOffset();

		offset.left += event.pointerX();
		offset.top += event.pointerY();

		Global.trace("frame = ", frame.id, "offset = ", offset);

		var panel = this.panels.detect(function(panel) {
			var f = $(panel.document.frame);
			var o = f.cumulativeOffset();
			var z = f.getDimensions();

			if (o.left < offset.left
					&& offset.left < o.left + z.width) {
				if (o.top < offset.top
						&& offset.top < o.top + z.height) {
					return true;
				}
			}

			return false;
		});

		if (panel) {
			Global.trace("frame = " + panel.document.frame.id);
		}
		
		return { panel: panel, offset: offset };
	},
	dragMove : function(event) {
		event.stop();
		
		this.findPanel(event);
//
//		if( result.panel ) {
//			var o = result.panel.document.frame.cumulativeOffset();
//
//			offset.left -= o.left;
//			offset.top -= o.top;
//		}
		var target = this.source.document.frame || event.target;
		var offset = $(target).cumulativeOffset();

		this.source.selected.each(function(shape) {
			shape.follow(event, offset);
		});
	},
	dragDone : function(event) {
		event.stop();

		if (this.source) {
			var result = this.findPanel(event);
			
			this.source.selected.each(function(shape) {
				shape.drop(result.panel, result.offset);
			});

			this.source = null;
		}

		this.bindMove(false);
	},
	bindMove : function(set) {
		if (set) {
			this.document.onmousemove = this.dragMove
					.bindAsEventListener(this);
		} else {
			this.document.onmousemove = null;
		}

		this.panels.each(function(panel) {
			panel.document.onmousemove = this.document.onmousemove;
		});
	},
	bindDone : function() {
		this.document.onmouseup = this.dragDone
				.bindAsEventListener(this);

		this.panels.each(function(panel) {
			panel.document.onmouseup = this.document.onmouseup;
		});

		this.document.onmouseout = this.mouseOut
				.bindAsEventListener(this);
	},
	mouseOut : function(event) {
		if (event.target == this.document.documentElement) {
			Global.trace("mouseOut", event.target);

			this.dragDone(event);
		}
	}
});

var Panel = Class.create(Base, {
	shapes : $A(),
	selected : $A(),

	initialize : function($super, frame, count) {
		var doc = frame.contentWindow.document;
		var obj = $super(doc, doc.body);

		doc.frame = frame;
		obj.className = 'panel';

		for ( var k = 0; k < count; k++) {
			obj.addShape( 20, 4 + k * 24);
		}

		return obj;
	},
	addShape: function(x, y) {
		var shape = new Moveable(this);
		
		this.shapes.push(shape);
		
		shape.moveTo(x, y);
	},
	removeShape: function(shape) {
		var ix = this.shapes.indexOf(shape);

		this.shapes[ix] = null;
		this.shapes = this.shapes.compact();

		this.removeChild(shape);
	},
	select : function(shape, control) {
		if (control) {
			shape.selectNow(!shape.selected());

			var ix = this.selected.indexOf(shape);

			if (ix < 0) {
				this.selected.push(shape);
			} else {
				this.selected[ix] = null;

				this.selected = this.selected.compact();
			}
		} else {
			this.selected.each(function(shape) {
				shape.selectNow(false);
			});

			this.selected = $A();

			this.selected.push(shape);

			shape.selectNow(true);
		}
	},
});

var Shape = Class.create(Base, {
	initialize : function($super, parent, style) {
		var obj = $super(parent.document, parent.document.createElement('div'));

		obj.className = style;
		obj.style.position = 'absolute';

		parent.appendChild(obj);

		return obj;
	},
	moveTo : function(x, y) {
		this.style.left = x + 'px';
		this.style.top = y + 'px';
	},
});

var Shadow = Class.create(Shape, {
	dx : null,
	dy : null,

	initialize : function($super, dx, dy) {
		var obj = $super(Global.top, 'shape_shadow');

		obj.dx = dx;
		obj.dy = dy;

		return obj;
	},
});

var Moveable = Class.create(Shape, {
	shadow : null,
	selectId : null,

	initialize : function($super, panel) {
		var obj = $super(panel, 'shape');

		obj.bindDown(true);
		obj.bindDone(true);

		return obj;
	},
	mouseMove : function(event) {
		event.stop();

		window.clearTimeout(this.selectId);

		this.bindMove(false);

		if (this.selected()) {
			Global.top.dragInit(this.parentElement, event);
		}
	},
	mouseDown : function(event) {
		event.stop();

		if (event.isLeftClick()) {
			// this.parentElement.select(this, event.ctrlKey);
			this.bindMove(true);

			this.selectId = this.select.delay(0.2, this, event.ctrlKey);
		}
	},
	mouseDone : function(event) {
		event.stop();

		this.bindMove(false);
	},
	select : function(shape, control) {
		Global.trace("select");

		shape.parentElement.select(shape, control);
		shape.bindMove(false);
	},
	selectNow : function(set) {
		if (set) {
			this.className = 'shape_selected';
		} else {
			this.className = 'shape';
		}
	},
	selected : function() {
		return this.className == 'shape_selected';
	},
	follow : function(event, offset) {
		var x = event.pointerX() - this.shadow.dx;
		var y = event.pointerY() - this.shadow.dy;

		if (offset) {
			x += offset.left;
			y += offset.top;
		}

		this.shadow.moveTo(x, y);
	},
	float : function(event) {
		var offset = this.cumulativeOffset();

		var dx = event.pointerX() - offset.left;
		var dy = event.pointerY() - offset.top;

		Global.trace("dx = ", dx, "dy = ", dy);

		this.shadow = new Shadow(dx, dy);

		this.follow(event);
	},
	drop : function(panel, offset) {
		var x = offset.left - this.shadow.dx;
		var y = offset.top - this.shadow.dy;

		if( panel ) {
			var o = panel.document.frame.cumulativeOffset();

			x -= o.left;
			y -= o.top;
		}
		
		Global.trace("x = ", x, ", y = ", y);

		if (panel == this.parentElement) {
			this.moveTo(x, y);
		}
		else if (panel) {
			panel.addShape(x, y);

			this.parentElement.removeShape(this);
		}

		Global.top.removeChild(this.shadow);

		this.shadow = null;
	}
});
