/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 *
 */
package net.sourceforge.plantuml.activitydiagram3.ftile.vcompact;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.plantuml.ColorParam;
import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.activitydiagram3.ftile.AbstractConnection;
import net.sourceforge.plantuml.activitydiagram3.ftile.Arrows;
import net.sourceforge.plantuml.activitydiagram3.ftile.Connection;
import net.sourceforge.plantuml.activitydiagram3.ftile.ConnectionTranslatable;
import net.sourceforge.plantuml.activitydiagram3.ftile.Ftile;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileAssemblySimple;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileGeometry;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileUtils;
import net.sourceforge.plantuml.activitydiagram3.ftile.Snake;
import net.sourceforge.plantuml.activitydiagram3.ftile.Swimlane;
import net.sourceforge.plantuml.activitydiagram3.ftile.vertical.FtileBlackBlock;
import net.sourceforge.plantuml.activitydiagram3.ftile.vertical.FtileDiamond;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.HtmlColorAndStyle;
import net.sourceforge.plantuml.graphic.Rainbow;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UPolygon;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class ParallelBuilderMerge extends ParallelFtilesBuilder {

	public ParallelBuilderMerge(ISkinParam skinParam, StringBounder stringBounder,
			final List<Ftile> list, Ftile inner, Swimlane swimlane) {
		super(skinParam, stringBounder, list, inner, swimlane);
	}

	@Override
	protected Ftile doStep1() {
		Ftile result = getMiddle();
		final List<Connection> conns = new ArrayList<Connection>();
		final HtmlColor colorBar = getRose().getHtmlColor(skinParam(), ColorParam.activityBar);

		final Ftile black = new FtileBlackBlock(skinParam(), colorBar, getList().get(0).getSwimlaneIn());
		double x = 0;
		for (Ftile tmp : getList()) {
			final Dimension2D dim = tmp.calculateDimension(getStringBounder());
			conns.add(new ConnectionIn(black, tmp, x, tmp.getInLinkRendering().getRainbow(
					HtmlColorAndStyle.build(skinParam()))));
			x += dim.getWidth();
		}

		result = FtileUtils.addConnection(result, conns);
		((FtileBlackBlock) black).setBlackBlockDimension(result.calculateDimension(getStringBounder()).getWidth(), barHeight);

		return new FtileAssemblySimple(black, result);
	}

	@Override
	protected  Ftile doStep2(Ftile result) {
		final HtmlColor borderColor = getRose().getHtmlColor(skinParam(), ColorParam.activityDiamondBorder);
		final HtmlColor backColor = getRose().getHtmlColor(skinParam(), ColorParam.activityDiamondBackground);
		final Ftile out = new FtileDiamond(skinParam(), backColor, borderColor, swimlane());
		result = new FtileAssemblySimple(result, out);
		final List<Connection> conns = new ArrayList<Connection>();
		final UTranslate diamondTranslate = result.getTranslateFor(out, getStringBounder());
		int i = 0;
		double x = 0;
		for (Ftile tmp : getList()) {
			final Dimension2D dim = tmp.calculateDimension(getStringBounder());
			final UTranslate translate0 = new UTranslate(x, barHeight);
			conns.add(new ConnectionHorizontalThenVertical(tmp, out, tmp.getOutLinkRendering().getRainbow(
					HtmlColorAndStyle.build(skinParam())), translate0, diamondTranslate, i));
			x += dim.getWidth();
			i++;
		}
		return FtileUtils.addConnection(result, conns);
	}

	class ConnectionHorizontalThenVertical extends AbstractConnection /* implements ConnectionTranslatable */{

		private final Rainbow arrowColor;
		private final UTranslate diamondTranslate;
		private final UTranslate translate0;
		private final int counter;

		public ConnectionHorizontalThenVertical(Ftile tile, Ftile diamond, Rainbow arrowColor, UTranslate translate0,
				UTranslate diamondTranslate, int counter) {
			super(tile, diamond);
			this.arrowColor = arrowColor;
			this.diamondTranslate = diamondTranslate;
			this.translate0 = translate0;
			this.counter = counter;
		}

		public void drawU(UGraphic ug) {
			final StringBounder stringBounder = ug.getStringBounder();
			final Point2D p1 = getP1(stringBounder);
			final Point2D p2 = getP2(stringBounder);
			final double x1 = p1.getX();
			final double y1 = p1.getY();
			final double x2 = p2.getX();
			final double y2 = p2.getY();

			UPolygon endDecoration = null;
			if (counter == 0) {
				endDecoration = Arrows.asToRight();
			} else if (counter == 1) {
				endDecoration = Arrows.asToLeft();
			}
			final Snake snake = new Snake(arrowHorizontalAlignment(), arrowColor, endDecoration);
			snake.addPoint(x1, y1);
			snake.addPoint(x1, y2);
			snake.addPoint(x2, y2);

			ug.draw(snake);
		}

		private Point2D getP1(StringBounder stringBounder) {
			return translate0.getTranslated(getFtile1().calculateDimension(stringBounder).getPointOut());
		}

		private Point2D getP2(final StringBounder stringBounder) {
			final Point2D result = diamondTranslate.getTranslated(getFtile2().calculateDimension(stringBounder)
					.getPointOut());
			final Dimension2D dim = getFtile2().calculateDimension(stringBounder);
			UTranslate arrival = new UTranslate();
			if (counter == 0) {
				arrival = new UTranslate(-dim.getWidth() / 2, -dim.getHeight() / 2);
			} else if (counter == 1) {
				arrival = new UTranslate(dim.getWidth() / 2, -dim.getHeight() / 2);
			}
			return arrival.getTranslated(result);
		}

	}

	class ConnectionIn extends AbstractConnection implements ConnectionTranslatable {

		private final double x;
		private final Rainbow arrowColor;
		private final Display label;

		public ConnectionIn(Ftile ftile1, Ftile ftile2, double x, Rainbow arrowColor) {
			super(ftile1, ftile2);
			label = ftile2.getInLinkRendering().getDisplay();
			this.x = x;
			this.arrowColor = arrowColor;
		}

		public void drawU(UGraphic ug) {
			ug = ug.apply(new UTranslate(x, 0));
			final FtileGeometry geo = getFtile2().calculateDimension(getStringBounder());
			final Snake snake = new Snake(arrowHorizontalAlignment(), arrowColor, Arrows.asToDown());
			if (Display.isNull(label) == false) {
				snake.setLabel(getTextBlock(label));
			}
			snake.addPoint(geo.getLeft(), 0);
			snake.addPoint(geo.getLeft(), geo.getInY());
			ug.draw(snake);
		}

		public void drawTranslate(UGraphic ug, UTranslate translate1, UTranslate translate2) {
			ug = ug.apply(new UTranslate(x, 0));
			final FtileGeometry geo = getFtile2().calculateDimension(getStringBounder());
			final Point2D p1 = new Point2D.Double(geo.getLeft(), 0);
			final Point2D p2 = new Point2D.Double(geo.getLeft(), geo.getInY());

			final Snake snake = new Snake(arrowHorizontalAlignment(), arrowColor, Arrows.asToDown());
			if (Display.isNull(label) == false) {
				snake.setLabel(getTextBlock(label));
			}
			final Point2D mp1a = translate1.getTranslated(p1);
			final Point2D mp2b = translate2.getTranslated(p2);
			final double middle = mp1a.getY() + 4;
			snake.addPoint(mp1a);
			snake.addPoint(mp1a.getX(), middle);
			snake.addPoint(mp2b.getX(), middle);
			snake.addPoint(mp2b);
			ug.draw(snake);
		}
	}

}
