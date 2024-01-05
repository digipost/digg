/*
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.jdbc;

import com.mockrunner.mock.jdbc.MockResultSet;
import no.digipost.tuple.Tuple;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static no.digipost.DiggExceptions.getUnchecked;
import static no.digipost.DiggExceptions.runUnchecked;

final class ResultSetMock extends MockResultSet {

    public static final ResultSetMock mockSingleColumnResult(String column, Object ... result) {
        return mockResult(Tuple.of(column, asList(result)));
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static final ResultSetMock mockSingleRowResult(Tuple<String, Object> ... columns) {
        return mockResultSet(Stream.of(columns).map(c -> c.mapSecond(Arrays::asList)));
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static final ResultSetMock mockResult(Tuple<String, List<Object>> ... columns) {
        return mockResultSet(Stream.of(columns));
    }

    public static final ResultSetMock mockResultSet(Stream<Tuple<String, List<Object>>> columns) {
        return getUnchecked(() -> {
            ResultSetMock rs = new ResultSetMock("mock-" + LocalDate.now());
            columns.forEach(column -> rs.addColumn(column.first(), column.second()));
            rs.next();
            return rs;
        });
    }



    private ResultSetMock(String id) {
        super(id);
    }

    @Override
    public void close() {
        runUnchecked(super::close);
    }


}
