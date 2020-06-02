import React from 'react'; 
import ReactDOM from 'react-dom'; 
import client from './client'; 

class MerchantsList extends React.Component {
  render() {
    const listOfMerchants = this.props.shops.map(
      merchant => <Merchant key={merchant._links.self.href} merchant ={merchant} />);
      return (
        <table>
          <tbody>
            {listOfMerchants};
          </tbody>
        </table>
      )
    }
}

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {shops: []};
  }

  componentDidMount() {
    client({
      method: 'GET', 
      path: '/'
    }).done( response => {
      this.setState({shops: response.entity});
    });
  }

  render() {
    return (
      <h1> Inside app.js</h1>
    )
  }
}

ReactDOM.render(<App/>, document.getElementById('react'));