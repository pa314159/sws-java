var Global = {};

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

Global.init = function() {
	Global.top = new Top(window.document);
};

Global.top = null;

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

var Top = Class.create(Base,
		{
			source : null,
			boards : $A(),

			initialize : function($super, document) {
				var obj = $super(document, document.body);

				$$('.frame').each(function(frame) {
					obj.boards.push(new Board(frame, 5));
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
			dragMove : function(event) {
				event.stop();

				var target = this.source.document.frame || event.target;
				var offset = $(target).cumulativeOffset();

				this.source.selected.each(function(shape) {
					shape.follow(event, offset);
				});
			},
			dragDone : function(event) {
				event.stop();

				if (this.source) {
					var frame = $(event.target.frame);
					var offset = frame.cumulativeOffset();

					offset.left += event.pointerX();
					offset.top += event.pointerY();

					Global.trace("dropped at " + offset);

					var board = this.boards.detect(function(board) {
						var f = $(board.document.frame);
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

					if (board) {
						frame = board.document.frame;
						
						var o = frame.cumulativeOffset();
						
						offset.left -= o.left;
						offset.top -= o.top;
					}

					this.source.selected.each(function(shape) {
						shape.drop(board, offset);
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

				this.boards.each(function(board) {
					board.document.onmousemove = this.document.onmousemove;
				});
			},
			bindDone : function() {
				this.document.onmouseup = this.dragDone
						.bindAsEventListener(this);

				this.boards.each(function(board) {
					board.document.onmouseup = this.document.onmouseup;
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

var Shape = Class.create(Base,
		{
			initialize : function($super, parent, style) {
				var obj = $super(parent.document, parent.document
						.createElement('div'));

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

var Board = Class.create(Base, {
	shapes : $A(),
	selected : $A(),

	initialize : function($super, frame, count) {
		var doc = frame.contentWindow.document;
		var obj = $super(doc, doc.body);

		doc.frame = frame;
		obj.className = 'board';

		for ( var k = 0; k < count; k++) {
			var shape = new Moveable(obj);

			shape.moveTo(20, 4 + k * 24);

			obj.shapes.push(shape);
		}

		return obj;
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

var Moveable = Class.create(Shape, {
	shadow : null,
	selectId : null,

	initialize : function($super, board) {
		var obj = $super(board, 'shape');

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

		// var offset = this.cumulativeOffset();
		// var dx = event.pointerX() - offset.left;
		// var dy = event.pointerY() - offset.top;
		//		
		// Global.trace("dx = ", dx, "dy = ", dy);
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
		// Global.trace("target = ", target.id, ", offset = ", offset,
		// ", pointerX = ", event.pointerX(), ", pointerY = ", event
		// .pointerY());

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
	drop : function(board, offset) {
		var x = offset.left - this.shadow.dx;
		var y = offset.top - this.shadow.dy;

		Global.trace("x = ", x, ", y = ", y);

		if (board == this.parentElement) {
			this.moveTo(x, y);
		}
		else if( board ) {
			var shape = new Moveable(board);
			
			board.shapes.push(shape);
			
			shape.moveTo(x, y);
			
			this.parentElement.removeChild(this);
		}

		Global.top.removeChild(this.shadow);

		this.shadow = null;
	}
});
