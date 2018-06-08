import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { TextField, Paper } from '@material-ui/core';
import 'typeface-roboto';

const styles = theme => ({
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit / 2,
        width: 'fill-available'
    }
});

class SearchBox extends Component {
    render() {
        const { classes } = this.props;

        return (
            <Paper className={this.props.className}>
                <TextField value={this.props.value}
                    onKeyPress={this.props.onSearchSubmit}
                    onChange={this.props.onSearchChange}
                    InputProps={{disableUnderline: true}}
                    id="search"
                    type="search"
                    className={ classes.textField }
                    margin="normal"
                    autoComplete="off">
                </TextField>
            </Paper>
        )
    }
}

SearchBox.propTypes = {
    classes: PropTypes.object.isRequired
};

export default withStyles(styles)(SearchBox);