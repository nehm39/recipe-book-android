using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Web;

namespace PrzepisyService.Data
{
    [DataContract]
    public class Category
    {
        [DataMember(Order = 0)]
        public int Id { get; set; }

        [DataMember(Order = 1)]
        public string Name { get; set; }

        [DataMember(Order = 2)]
        public char Modifier { get; set; }

        [DataMember(Order = 3)]
        public int Version { get; set; }
    }
}