/*
 * #%L
 * GraphWalker Dashboard
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
/* Bulgarian initialisation for the jQuery UI date picker plugin. */
/* Written by Stoyan Kyosev (http://svest.org). */
jQuery(function($){
	$.datepicker.regional['bg'] = {
		closeText: 'затвори',
		prevText: '&#x3C;назад',
		nextText: 'напред&#x3E;',
		nextBigText: '&#x3E;&#x3E;',
		currentText: 'днес',
		monthNames: ['Януари','Февруари','Март','Април','Май','Юни',
		'Юли','Август','Септември','Октомври','Ноември','Декември'],
		monthNamesShort: ['Яну','Фев','Мар','Апр','Май','Юни',
		'Юли','Авг','Сеп','Окт','Нов','Дек'],
		dayNames: ['Неделя','Понеделник','Вторник','Сряда','Четвъртък','Петък','Събота'],
		dayNamesShort: ['Нед','Пон','Вто','Сря','Чет','Пет','Съб'],
		dayNamesMin: ['Не','По','Вт','Ср','Че','Пе','Съ'],
		weekHeader: 'Wk',
		dateFormat: 'dd.mm.yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['bg']);
});
