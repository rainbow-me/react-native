/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 * @oncall react_native
 */

const React = require('react');
import Text from '../../../Text/Text';
const LogBoxInspectorSection = require('../LogBoxInspectorSection').default;
const render = require('../../../../jest/renderer');

describe('LogBoxInspectorSection', () => {
  it('should render with only heading', () => {
    const output = render.shallowRender(
      <LogBoxInspectorSection heading="Test Section">
        <Text>Child</Text>
      </LogBoxInspectorSection>,
    );

    expect(output).toMatchSnapshot();
  });

  it('should render with action on the right', () => {
    const output = render.shallowRender(
      <LogBoxInspectorSection
        heading="Test Section"
        action={<Text>Right</Text>}>
        <Text>Child</Text>
      </LogBoxInspectorSection>,
    );

    expect(output).toMatchSnapshot();
  });
});
