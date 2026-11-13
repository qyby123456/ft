import {rooms, buildRoom} from './forest.mjs'
export default api => buildRoom(api, rooms.find(room => room.id === 'nature-b1-s1-02'))
