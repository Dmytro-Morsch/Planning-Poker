import {Route, Routes} from "react-router-dom";
import {UserVoteProvider} from "./context/UserVote.context.jsx";

import {Home, Room} from './components';

import './App.css';

function App() {
    return (
        <>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/room/:roomId" element={<UserVoteProvider><Room/></UserVoteProvider>}/>
                <Route path="*" element={<h1>Not Found</h1>}/>
            </Routes>
        </>
    );
}

export default App;
